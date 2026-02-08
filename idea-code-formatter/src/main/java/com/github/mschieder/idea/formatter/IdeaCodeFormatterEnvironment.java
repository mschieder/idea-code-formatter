package com.github.mschieder.idea.formatter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IdeaCodeFormatterEnvironment implements AutoCloseable {

    private static final Logger log = Logger.getLogger(IdeaCodeFormatterEnvironment.class.getName());
    private final Path tmpFormatterRoot;
    private final Path appdata;
    private final Path localAppdata;
    private final Path userHome;
    private final Path ideaConfigPath;
    private final Path ideaPluginsPath;
    private final Path ideaSystemPath;
    private final Path ideaLogPath;

    enum ClasspathType {
        IDEA_LIB("idea/lib"), IDEA_FULL("idea");
        private final String dir;

        ClasspathType(String dir) {
            this.dir = dir;
        }

        public String getDir() {
            return dir;
        }
    }

    public IdeaCodeFormatterEnvironment() throws IOException {
        tmpFormatterRoot = extractPortableIde();
        userHome = Files.createDirectories(tmpFormatterRoot.resolve("userHome"));
        appdata = Files.createDirectories(tmpFormatterRoot.resolve("appdata"));
        localAppdata = Files.createDirectories(tmpFormatterRoot.resolve("localAppdata"));
        ideaConfigPath = Files.createDirectories(tmpFormatterRoot.resolve("ideaConfig"));
        ideaPluginsPath = Files.createDirectories(tmpFormatterRoot.resolve("ideaPlugins"));
        ideaSystemPath = Files.createDirectories(tmpFormatterRoot.resolve("ideaSystem"));
        ideaLogPath = Files.createDirectories(tmpFormatterRoot.resolve("ideaLog"));
    }

    @Override
    public void close() throws Exception {
        Utils.deleteDir(tmpFormatterRoot);
    }

    private Path extractPortableIde() throws IOException {
        Path tmpFormatterRoot = Files.createTempDirectory("formatterRoot");
        InputStream f = IdeaCodeFormatterMain.class.getResourceAsStream("/idea.zip");
        Utils.unzipZippedFileFromResource(f, tmpFormatterRoot);
        return tmpFormatterRoot;
    }

    public FormatterResult format(String[] args) throws Exception {
        return this.format(args, outputLines -> outputLines.forEach(System.out::println));
    }

    public FormatterResult format(String[] args, Consumer<List<String>> outputLinePrinter) throws Exception {
        List<String> outputLines = new ArrayList<>();
        FormatterResult result =
                doFormat(tmpFormatterRoot, args, outputLines);
        outputLinePrinter.accept(outputLines);
        return result;
    }

    public FormatterResult validate(String[] args, Consumer<List<String>> outputLinePrinter) throws Exception {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        if (!argsList.contains("-d") && !argsList.contains("-dry")) {
            argsList.add(0, "-dry");
        }

        List<String> outputLines = new ArrayList<>();
        FormatterResult result = doFormat(tmpFormatterRoot, argsList.toArray(new String[0]), outputLines);

        boolean validationOk = true;
        outputLinePrinter.accept(outputLines);

        for (String line : outputLines) {
            if (line.contains("...Needs reformatting")) {
                validationOk = false;
                break;
            }
        }

        if (result.exitCode() == 0) {
            return validationOk ? result : result.withExitCode(-1);
        }
        return result;
    }

    private String buildClasspath(ClasspathType classpathType) {
        List<String> classpath = new ArrayList<>();
        if (!Utils.isPackagedInJar()) {
            // add the classpath to run inside IDEA
            classpath.add(Utils.class.getProtectionDomain().getCodeSource().getLocation().toString());
        }
        try (var allFiles = Files.walk(tmpFormatterRoot.resolve(classpathType.getDir()))) {
            allFiles.map(Path::toString)
                    .filter(string -> string.endsWith(".jar"))
                    .sorted(Comparator.reverseOrder())
                    .forEach(classpath::add);
            log.fine(("built classpath: " + classpath));
            return String.join(File.pathSeparatorChar + "", classpath);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private FormatterResult doFormat(Path formatterRoot, String[] args, List<String> outputLines) throws Exception {
        if (this.getClass().getResource("/dev.properties") != null) {
            Properties properties = new Properties();
            properties.load(this.getClass().getResourceAsStream("/dev.properties"));
            properties.forEach((k, v) -> System.setProperty(k.toString(), v.toString()));
        }

        String javaBin = System.getProperty("java.home") + "/bin/java";

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-cp");

        if (System.getProperty("idea.classpath.type", "FULL").equals("FULL")) {
            // idea libs and idea plugins are in classpath
            command.add(buildClasspath(ClasspathType.IDEA_FULL));
            // we don't need idea home path, everything is accessible via class path
            Path tmpHomeDir = Files.createTempDirectory("ideaHome");
            tmpHomeDir.toFile().deleteOnExit();
            command.add("-Didea.home.path=" + tmpHomeDir.toAbsolutePath());

        } else {
            // idea libs are in classpath, idea plugin are loaded from the plugins directory
            command.add(buildClasspath(ClasspathType.IDEA_LIB));
            // we need the idea home to get access to the plugin directory to load the jars later
            command.add("-Didea.home.path=" + tmpFormatterRoot.resolve("idea").toAbsolutePath());
        }

        // add all -D-xxxx system properties as vmargs (-D-X... => -X... , -D-Dxxx => -Dxxx)
        command.addAll(filterSystemProperties());

        // set user.home
        command.add("-Duser.home=" + userHome);
        // set idea paths
        command.add("-Didea.config.path=" + ideaConfigPath);
        command.add("-Didea.system.path=" + ideaSystemPath);
        command.add("-Didea.plugin.path=" + ideaPluginsPath);
        command.add("-Didea.log.path=" + ideaLogPath);

        if (System.getProperty("classloader.log.dir") != null) {
            // enable idea classpath logging
            command.add("-Dclassloader.log.dir=" + System.getProperty("classloader.log.dir"));
            command.add("-Djava.system.class.loader=com.github.mschieder.idea.formatter.LoggingClassLoader");
            command.add("-Didea.record.classpath.info=true");

        } else {
            command.add("-Djava.system.class.loader=com.intellij.util.lang.PathClassLoader");
        }


        command.add("-Didea.vendor.name=JetBrains");
        command.add("-Didea.paths.selector=IdeaFormatter");
        command.add("-Djna.nosys=true");
        command.add("-Djna.noclasspath=true");
        command.add("-Didea.platform.prefix=Idea");
        command.add("-Dsplash=false");

        // add all add-opens
        try (var reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/add-opens.txt"), StandardCharsets.UTF_8))) {
            command.addAll(reader.lines().toList());
        }

        command.add("com.intellij.idea.Main");

        final List<String> argList = Arrays.stream(args).map(String::trim).distinct().toList();
        if (!argList.contains("format")) {
            command.add("format");
        }
        command.addAll(argList);

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.environment().put("APPDATA", appdata.toString());
        builder.environment().put("LOCALAPPDATA", localAppdata.toString());

        long started = System.currentTimeMillis();
        final Path errorLog = formatterRoot.resolve("error.log");
        Process process = builder
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.to(errorLog.toFile()))
                .start();

        int exitCode;
        if (System.getProperty("idea.process.termination", "WAIT_FOR").equals("WAIT_FOR")) {
            // wait for "normal" process termination
            exitCode = process.waitFor();
            // read all output lines
            readlines(process, outputLines);
        } else {
            // wait for "normal" process termination or manually destroy the process after 2000ms after printing the success of the
            // formatting action
            exitCode = waitForProcess(process, outputLines);
        }

        // append idea errors to the output
        final boolean isDryRun = command.contains("-d") || command.contains("-dry");
        if ("always".equals(System.getProperty("idea.logErrors", "exitStatus"))
                || (!isDryRun && exitCode != 0 && Files.exists(errorLog))) {
            outputLines.addAll(Files.readAllLines(errorLog));
        }

        log.log(Level.FINE, "process finished after {0} ms", System.currentTimeMillis() - started);

        return FormatterResult.parse(outputLines).withExitCode(exitCode);

    }

    private int waitForProcess(Process process, List<String> outputLines) throws InterruptedException, IOException {
        int totalWaitTimeAfterSummaryInMillis = 0;
        boolean summaryPrinted = false;
        while (process.isAlive()) {
            process.waitFor(100, TimeUnit.MILLISECONDS);
            if (summaryPrinted) {
                totalWaitTimeAfterSummaryInMillis += 100;
            }

            if (process.isAlive()) {
                readlines(process, outputLines);
                summaryPrinted = summaryPrinted || FormatterResult.parse(outputLines).isReady();

            }
            if (summaryPrinted && totalWaitTimeAfterSummaryInMillis >= 2000) {
                //terminate after 2 seconds after the summary was printed
                process.destroyForcibly();
                log.warning("process destroyed after 2000 ms");

                // calculate the exit code from the summary
                FormatterResult result = FormatterResult.parse(outputLines);
                if (result.wellFormed() != -1 && result.checked() != result.wellFormed()) {
                    // dry run: not all files are well formed
                    return 1;
                }
                // all other use cases: summary printed => SUCCESS
                return 0;
            }
        }
        return process.exitValue();
    }

    private void readlines(Process process, List<String> outputLines) throws IOException {
        int availableBytes = process.getInputStream().available();
        if (availableBytes > 0) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(process.getInputStream().readNBytes(availableBytes))))) {
                reader.lines().forEach(outputLines::add);
            }
        }
    }


    private List<String> filterSystemProperties() {
        return System.getProperties().entrySet().stream().filter(entry -> entry.getKey().toString()
                .startsWith("-")).map(entry -> entry.getKey() + "=" + entry.getValue()).toList();

    }
}

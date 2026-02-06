package com.github.mschieder.idea.formatter;

import java.io.BufferedReader;
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
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IdeaCodeFormatterEnvironment implements AutoCloseable {

    private static final Logger log = Logger.getLogger(IdeaCodeFormatterEnvironment.class.getName());
    private final Path tmpFormatterRoot;

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

    public int format(String[] args) throws Exception {
        return this.format(args, outputLines -> outputLines.forEach(System.out::println));
    }

    public int format(String[] args, Consumer<List<String>> outputLinePrinter) throws Exception {
        List<String> outputLines = new ArrayList<>();
        int returnCode =
                doFormat(tmpFormatterRoot, args, outputLines);
        outputLinePrinter.accept(outputLines);
        return returnCode;
    }

    public int validate(String[] args) throws Exception {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        if (!argsList.contains("-d") && !argsList.contains("-dry")) {
            argsList.add(0, "-dry");
        }

        List<String> outputLines = new ArrayList<>();
        int returnCode = doFormat(tmpFormatterRoot, argsList.toArray(new String[0]), outputLines);

        boolean validationOk = true;
        for (String line : outputLines) {
            if (line.contains("...Needs reformatting")) {
                log.log(Level.SEVERE, line);
                validationOk = false;
            } else {
                log.info(line);
            }
        }

        if (returnCode == 0) {
            return validationOk ? 0 : -1;
        }
        return returnCode;
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
            return String.join(":", classpath);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int doFormat(Path formatterRoot, String[] args, List<String> outputLines) throws Exception {
        if (this.getClass().getResource("/dev.properties") != null) {
            Properties properties = new Properties();
            properties.load(this.getClass().getResourceAsStream("/dev.properties"));
            properties.forEach((k, v) -> System.setProperty(k.toString(), v.toString()));
        }

        String javaBin = System.getProperty("java.home") + "/bin/java";
        String appdata = formatterRoot.resolve("appdata").toString();
        String localAppdata = formatterRoot.resolve("localAppdata").toString();


        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-cp");

        if (System.getProperty("idea.classpath.type", "FULL").equals("FULL")) {
            // idea libs and idea plugins are in classpath
            command.add(buildClasspath(ClasspathType.IDEA_FULL));
            // we don't need idea home path, everything is accessible via class path
            var tmpHomeDir = Files.createTempDirectory("ideaHome");
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
        builder.environment().put("APPDATA", appdata);
        builder.environment().put("LOCALAPPDATA", localAppdata);

        long started = System.currentTimeMillis();
        final Path errorLog = formatterRoot.resolve("error.log");
        Process process = builder
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.to(errorLog.toFile()))
                .start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            process.waitFor();
            reader.lines().forEach(outputLines::add);
            log.log(Level.FINE, "process finished after {0} ms", System.currentTimeMillis() - started);
            int exitValue = process.exitValue();
            if ("always".equals(System.getProperty("idea.logErrors", "exitStatus"))
                    || (exitValue != 0 && Files.exists(errorLog))) {
                outputLines.addAll(Files.readAllLines(errorLog));
            }
            return exitValue;
        }
    }

    private List<String> filterSystemProperties() {
        return System.getProperties().entrySet().stream().filter(entry -> entry.getKey().toString()
                .startsWith("-")).map(entry -> entry.getKey() + "=" + entry.getValue()).toList();

    }
}

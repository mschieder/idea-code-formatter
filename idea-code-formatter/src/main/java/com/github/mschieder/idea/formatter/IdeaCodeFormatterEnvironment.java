package com.github.mschieder.idea.formatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class IdeaCodeFormatterEnvironment implements AutoCloseable {

    private static final Logger log = Logger.getLogger(IdeaCodeFormatterEnvironment.class.getName());
    private final Path tmpFormatterRoot;

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
        return this.format(args, outputLines -> outputLines.forEach(log::info));
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

    private int doFormat(Path formatterRoot, String[] args, List<String> outputLines) throws Exception {

        String javaBin = System.getProperty("java.home") + "/bin/java";

        String ideaHome = formatterRoot.resolve("idea").toString();
        String appdata = formatterRoot.resolve("appdata").toString();
        String localAppdata = formatterRoot.resolve("localAppdata").toString();

        String classpath = Files.list(tmpFormatterRoot.resolve("idea/lib"))
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .collect(Collectors.joining(":"));

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-cp");

        command.add(classpath);

        command.add("-Didea.home.path=" + ideaHome);
        command.add("-Djava.system.class.loader=com.intellij.util.lang.PathClassLoader");
        command.add("-Didea.vendor.name=JetBrains");
        command.add("-Didea.paths.selector=IdeaIC2023.1");
        command.add("-Djna.nosys=true");
        command.add("-Djna.noclasspath=true");
        command.add("-Didea.platform.prefix=Idea");
        command.add("-Dsplash=false");


        command.add("--add-opens=java.base/java.io=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.lang.ref=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.lang.reflect=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.net=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.nio=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.nio.charset=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.text=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.time=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.util=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.util.concurrent=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED");
        command.add("--add-opens=java.base/jdk.internal.vm=ALL-UNNAMED");
        command.add("--add-opens=java.base/sun.nio.ch=ALL-UNNAMED");
        command.add("--add-opens=java.base/sun.nio.fs=ALL-UNNAMED");
        command.add("--add-opens=java.base/sun.security.ssl=ALL-UNNAMED");
        command.add("--add-opens=java.base/sun.security.util=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/java.awt=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/java.awt.dnd.peer=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/java.awt.event=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/java.awt.image=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/java.awt.peer=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/java.awt.font=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/javax.swing=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.awt.datatransfer=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.awt.image=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.awt.windows=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.awt=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.font=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.java2d=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.swing=ALL-UNNAMED");
        command.add("--add-opens=jdk.attach/sun.tools.attach=ALL-UNNAMED");
        command.add("--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED");
        command.add("--add-opens=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED");
        command.add("--add-opens=jdk.jdi/com.sun.tools.jdi=ALL-UNNAMED");


        command.add("com.intellij.idea.Main");
        command.add("format");
        command.addAll(Arrays.asList(args));

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.environment().put("APPDATA", appdata);
        builder.environment().put("LOCALAPPDATA", localAppdata);


        long started = System.currentTimeMillis();
        Process process = builder
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.to(formatterRoot.resolve("error.log").toFile()))
                .start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            process.waitFor();
            reader.lines().forEach(outputLines::add);
            process.waitFor();
            log.log(Level.FINE, "process finished after {0} ms", System.currentTimeMillis() - started);
            return process.exitValue();
        }
    }
}

package com.github.mschieder.idea.formatter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IdeaCodeFormatterEnvironment implements AutoCloseable {

    private static final String[] LIB_JARS = {
            "3rd-party-rt.jar",
            "app.jar",
            "external-system-rt.jar",
            "forms_rt.jar",
            "groovy.jar",
            "jps-model.jar",
            "jsp-base.jar",
            "protobuf.jar",
            "rd.jar",
            "stats.jar",
            "util.jar",
            "util_rt.jar",
            "xml-dom.jar",
            "xml-dom-impl.jar"
    };
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
        InputStream f = IdeaCodeFormatterMain.class.getResourceAsStream("/ide.zip");
        Utils.unzip(f, tmpFormatterRoot.toFile());
        return tmpFormatterRoot;
    }


    public int format(String[] args) throws Exception {
        System.out.println("JAR: " + Utils.getJarName());
        System.out.println("JAR-PATH: " + new File(Utils.getJarPath(Utils.class)));
        return doFormat(tmpFormatterRoot, args);
    }

    private int doFormat(Path formatterRoot, String[] args) throws Exception {

        String javaBin = System.getProperty("java.home") + "/bin/java";

        String ideHome = formatterRoot.resolve("ide").toString();
        String appdata = formatterRoot.resolve("appdata").toString();
        String localAppdata = formatterRoot.resolve("localAppdata").toString();

        String classpathSeparator = System.getProperty("path.separator");

        String classpath = null;
        if (Utils.isPackagedInJar()) {
            classpath = new File(Utils.getJarPath(Utils.class)).toString();
        } else {
            // add the current classpath (for unit testing)
            classpath = Arrays.stream(LIB_JARS).map(j -> ideHome + "/lib/" + j).collect(Collectors.joining(classpathSeparator));
            classpath = System.getProperty("java.class.path");
        }

        String mainClass = "com.intellij.idea.Main";

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-cp");
        command.add(classpath);

        command.add("-Didea.home.path=" + ideHome);
        command.add("-Djava.system.class.loader=com.intellij.util.lang.PathClassLoader");
        command.add("-Didea.vendor.name=JetBrains");
        command.add("-Didea.paths.selector=IdeaIC2022.3");
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


        command.add(mainClass);
        command.add("format");
        command.addAll(Arrays.asList(args));

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.environment().put("APPDATA", appdata);
        builder.environment().put("LOCALAPPDATA", localAppdata);

        Process process = builder
                //       .inheritIO()
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.to(formatterRoot.resolve("error.log").toFile()))
                .start();
        process.waitFor();
        return process.exitValue();
    }
}

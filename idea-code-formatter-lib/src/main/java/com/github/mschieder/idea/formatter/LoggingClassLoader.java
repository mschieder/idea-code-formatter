package com.github.mschieder.idea.formatter;


import com.intellij.util.lang.ClassPath;
import com.intellij.util.lang.PathClassLoader;
import com.intellij.util.lang.UrlClassLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

public class LoggingClassLoader extends ClassLoader {

    private static final Logger log = Logger.getLogger(LoggingClassLoader.class.getName());

    public LoggingClassLoader(ClassLoader parent) {
        super(new PathClassLoader(UrlClassLoader.build().get()));

        if (LogLoadedClasses.getInstance().isEnabled() && Boolean.getBoolean("idea.record.classpath.info")) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    log.info("total number of loaded classes and resources: " + ClassPath.getLoadedClasses().size());
                    Files.write(Path.of(LogLoadedClasses.getInstance().getLogDir() + "/idea_loaded_classes_and_resources.txt"),
                            ClassPath.getLoadedClasses().stream().map(e -> e.getKey() + " " + e.getValue()).toList(), StandardOpenOption.APPEND);
                } catch (Throwable e) {
                    log.severe("error while proc " + e.getMessage());
                }
            }));
        }
    }

}

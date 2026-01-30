package com.github.mschieder.idea.formatter;

public class LogLoadedClasses {

    private final static LogLoadedClasses instance = new LogLoadedClasses();

    private LogLoadedClasses() {

    }

    public static LogLoadedClasses getInstance() {
        return instance;
    }

    public boolean isEnabled() {
        return System.getProperty("classloader.log.dir") != null;
    }

    public String getLogDir() {
        String logDir = System.getProperty("classloader.log.dir");
        return logDir.endsWith("/") ? logDir.substring(0, logDir.length() - 1) : logDir;
    }

}

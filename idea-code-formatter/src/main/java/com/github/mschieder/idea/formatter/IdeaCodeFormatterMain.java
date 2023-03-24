package com.github.mschieder.idea.formatter;


public class IdeaCodeFormatterMain {

    public static void main(String[] args) throws Exception {
        int exitStatus;
        try (IdeaCodeFormatterEnvironment formatter = new IdeaCodeFormatterEnvironment()) {
            exitStatus = formatter.format(args);
        }
        System.exit(exitStatus);
    }

}
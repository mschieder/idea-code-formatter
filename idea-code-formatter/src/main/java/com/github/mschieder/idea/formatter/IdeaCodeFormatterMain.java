package com.github.mschieder.idea.formatter;


public class IdeaCodeFormatterMain {

    public static void main(String[] args) throws Exception {
        FormatterResult result;
        try (IdeaCodeFormatterEnvironment formatter = new IdeaCodeFormatterEnvironment()) {
            result = formatter.format(args, lines -> lines.forEach(System.out::println));
        }
        System.exit(result.exitCode());
    }

}
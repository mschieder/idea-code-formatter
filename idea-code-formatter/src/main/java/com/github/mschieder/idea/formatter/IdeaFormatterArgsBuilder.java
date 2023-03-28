package com.github.mschieder.idea.formatter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IdeaFormatterArgsBuilder {

    private boolean dryRun = false;
    private File codestyleSettingsFile;

    private List<String> masks = new ArrayList<>();
    private String charset;
    private List<File> directories = new ArrayList<>();


    public IdeaFormatterArgsBuilder directories(List<File> directories) {
        this.directories.addAll(directories.stream().filter(File::exists).toList());
        return this;
    }


    public IdeaFormatterArgsBuilder dryRun(boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }

    public IdeaFormatterArgsBuilder codestyleSettingsFile(File codestyleSettingsFile) {
        this.codestyleSettingsFile = codestyleSettingsFile;
        return this;
    }

    public IdeaFormatterArgsBuilder charset(String charset) {
        this.charset = charset;
        return this;
    }

    public IdeaFormatterArgsBuilder masks(String... mask){
        return masks(Arrays.asList(mask));
    }

    public IdeaFormatterArgsBuilder masks(List<String> masks){
        this.masks.addAll(masks);
        return this;
    }

    public String[] build() {
        List<String> args = new ArrayList<>();
        if (dryRun) {
            args.add("-dry");
        }
        if (!masks.isEmpty()){
            args.add("-mask");
            args.add(masks.stream().collect(Collectors.joining(",")));
        }
        if (charset != null) {
            args.add("-charset");
            args.add(charset);
        }
        if (codestyleSettingsFile != null) {
            args.add("-settings");
            args.add(codestyleSettingsFile.toString());
        } else {
            args.add("-allowDefaults");
        }

        //path(s)
        directories.stream().map(File::toString).forEach(args::add);

        return args.toArray(new String[0]);
    }
}

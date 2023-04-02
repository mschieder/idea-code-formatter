package com.github.mschieder.idea.formatter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

@Mojo(name = "format", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class FormatMojo extends AbstractMojo {
    /**
     * Perform a dry run: no file modifications, only exit status.
     */
    @Parameter(property = "format.dryRun", defaultValue = "false")
    private boolean dryRun;

    /**
     * Scan directories recursively.
     */
    @Parameter(property = "format.recursive", defaultValue = "true")
    private boolean recursive;

    @Parameter(property = "format.masks", defaultValue = "*.java")
    private List<String> masks;

    @Parameter(property = "format.codestyleSettingsFile")
    private File codestyleSettingsFile;

    @Parameter(property = "format.directories", defaultValue = "src/main/java,src/test/java")
    private List<File> directories;

    /**
     * Force charset to use when reading and writing files.
     */
    @Parameter(property = "format.charset")
    private String charset;

    public void execute() throws MojoExecutionException {
        getLog().debug("dryRun?: " + dryRun);
        getLog().debug("codestyleSettingsFile: " + codestyleSettingsFile);
        getLog().debug("directories: " + directories);
        getLog().debug("charset: " + charset);
        getLog().debug("masks: " + masks);


        try (IdeaCodeFormatterEnvironment environment = new IdeaCodeFormatterEnvironment()) {
            environment.format(new IdeaFormatterArgsBuilder().charset(charset).masks(masks).dryRun(dryRun).recursive(recursive)
                    .directories(directories).codestyleSettingsFile(codestyleSettingsFile).build());
        } catch (Exception e) {
            throw new MojoExecutionException(e);
        }
    }
}
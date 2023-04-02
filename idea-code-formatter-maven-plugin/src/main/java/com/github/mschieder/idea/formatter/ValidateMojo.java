package com.github.mschieder.idea.formatter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

@Mojo(name = "validate", defaultPhase = LifecyclePhase.VALIDATE)
public class ValidateMojo extends IdeaCodeFormatterMojo {
    @Parameter(property = "validate.masks", defaultValue = "*.java")
    private List<String> masks;

    @Parameter(property = "validate.codestyleSettingsFile")
    private File codestyleSettingsFile;

    @Parameter(property = "validate.directories", defaultValue = "src/main/java,src/test/java")
    private List<File> directories;

    /**
     * Force charset to use when reading and writing files.
     */
    @Parameter(property = "validate.charset")
    private String charset;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().debug("codestyleSettingsFile: " + codestyleSettingsFile);
        getLog().debug("directories: " + directories);
        getLog().debug("charset: " + charset);
        getLog().debug("masks: " + masks);

        var returnCode = -1;
        try (IdeaCodeFormatterEnvironment environment = new IdeaCodeFormatterEnvironment()){
            returnCode = environment.validate(new IdeaFormatterArgsBuilder().charset(charset).masks(masks)
                    .dryRun(true).directories(directories).codestyleSettingsFile(codestyleSettingsFile).build());
        } catch (Exception e) {
            throw new MojoExecutionException(e);
        }
        if (returnCode != 0) {
            throw new MojoExecutionException("some files are not clean.");
        }
    }
}
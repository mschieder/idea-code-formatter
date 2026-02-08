import com.github.mschieder.idea.formatter.FormatterResult;
import com.github.mschieder.idea.formatter.IdeaCodeFormatterEnvironment;
import com.github.mschieder.idea.formatter.Utils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;
import java.util.function.BiPredicate;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaFormatterTest {
    private static IdeaCodeFormatterEnvironment formatter;
    private static File javaFile;
    private static Path javaDir;

    @BeforeAll
    static void setup() throws Exception {
        formatter = new IdeaCodeFormatterEnvironment();
        javaFile = File.createTempFile("test", ".java");
        javaFile.deleteOnExit();
        javaDir = Files.createTempDirectory("testJavaDir");
    }

    @AfterAll
    static void tearDown() throws Exception {
        formatter.close();
        Utils.deleteDir(javaDir);
    }

    private byte[] givenJavaFile(String filename) throws Exception {
        return givenJavaFile("/testfiles/java/given/", filename);
    }

    private byte[] givenJavaFile(String path, String filename) throws Exception {
        Files.copy(this.getClass().getResourceAsStream(path + filename), javaFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        try (var inputStream = new FileInputStream(javaFile)) {
            // return the orginal bytes
            return inputStream.readAllBytes();
        }
    }

    private void givenJavaDirectoryWithFiles(String... filename) throws Exception {
        //copy all files
        for (String file : filename) {
            givenJavaFile(file);
            Path fileDir = Files.createDirectory(javaDir.resolve(UUID.randomUUID().toString()));
            Files.copy(javaFile.toPath(), fileDir.resolve(file), StandardCopyOption.REPLACE_EXISTING);
        }
    }


    public static String[] javaTestFiles() {
        return new String[]{"SimpleTestClass.java"};
    }

    @ParameterizedTest
    @MethodSource("javaTestFiles")
    void testReformatJavaDefaultOk(String filename) throws Exception {
        // String filename = "SimpleTestClass.java";
        givenJavaFile(filename);
        formatter.format(new String[]{"-allowDefaults", javaFile.toString()});
        //reload file
        assertJavaDefaultsSame(filename);
    }

    @ParameterizedTest
    @MethodSource("javaTestFiles")
    void testReformatJavaDefaultDryRunNeedsReformattingOk(String filename) throws Exception {
        byte[] originalContent = givenJavaFile(filename);
        FormatterResult result = formatter.format(new String[]{"-d", "-allowDefaults", javaFile.toString()});
        assertThat(result.exitCode()).isNotEqualTo(0); // Needs reformatting
        assertThat(javaFile).hasBinaryContent(originalContent);
    }

    @ParameterizedTest
    @MethodSource("javaTestFiles")
    void testReformatJavaDefaultDryRunWellFormattedOk(String filename) throws Exception {
        byte[] originalContent = givenJavaFile("/testfiles/java/expected/defaults/", filename);
        FormatterResult result = formatter.format(new String[]{"-d", "-allowDefaults", javaFile.toString()});
        assertThat(result.exitCode()).isEqualTo(0); // asd
        assertThat(javaFile).hasBinaryContent(originalContent);
    }


    @ParameterizedTest
    @MethodSource("javaTestFiles")
    void testReformatJavaDefaultDirectoryMaskOk(String filename) throws Exception {
        givenJavaFile(filename);
        formatter.format(new String[]{"-allowDefaults", "-m", "*.java", javaFile.getParentFile().toString()});
        //reload file
        assertJavaDefaultsSame(filename);
    }

    @ParameterizedTest
    @MethodSource("javaTestFiles")
    void testReformatJavaDefaultDirectoryRecursiveMaskOk(String filename) throws Exception {
        // String filename = "SimpleTestClass.java";
        givenJavaDirectoryWithFiles(filename);
        formatter.format(new String[]{"-r", "-allowDefaults", "-m", "*.java", javaDir.toString()});
        //reload file
        assertJavaDefaultsDirContainsSame(filename);
    }

    void assertJavaDefaultsSame(String javaFilename) {
        Path expected = new File(this.getClass().getResource("/testfiles/java/expected/defaults/" + javaFilename).getFile()).toPath();
        assertThat(javaFile.toPath()).hasSameBinaryContentAs(expected);
    }

    void assertJavaDefaultsDirContainsSame(String javaFilename) throws Exception {
        Path expected = new File(this.getClass().getResource("/testfiles/java/expected/defaults/" + javaFilename).getFile()).toPath();
        assertThat(Files.find(javaDir, 3, (BiPredicate<Path, BasicFileAttributes>) (path, attr) -> Files.isRegularFile(path), FileVisitOption.FOLLOW_LINKS).findFirst().orElseThrow()).hasSameBinaryContentAs(expected);
    }


}

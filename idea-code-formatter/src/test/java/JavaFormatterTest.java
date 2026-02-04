import com.github.mschieder.idea.formatter.IdeaCodeFormatterEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaFormatterTest {
    private static IdeaCodeFormatterEnvironment formatter;
    private static File javaFile;

    @BeforeAll
    static void setup() throws Exception {
        formatter = new IdeaCodeFormatterEnvironment();
        javaFile = File.createTempFile("test", ".java");
        javaFile.deleteOnExit();
    }

    @AfterAll
    static void tearDown() throws Exception {
        formatter.close();
    }

    private void givenJavaFile(String filename) throws Exception {
        Files.copy(this.getClass().getResourceAsStream("/testfiles/java/given/" + filename), javaFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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

    void assertJavaDefaultsSame(String javaFilename) {
        Path expected = new File(this.getClass().getResource("/testfiles/java/expected/defaults/" + javaFilename).getFile()).toPath();
        assertThat(javaFile.toPath()).hasSameBinaryContentAs(expected);
    }


}

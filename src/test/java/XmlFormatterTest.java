import com.github.mschieder.idea.formatter.IdeaCodeFormatterEnvironment;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class XmlFormatterTest {

    private static IdeaCodeFormatterEnvironment formatter;
    private static File xmlFile;

    @BeforeAll
    static void setup() throws Exception{
        formatter =  new IdeaCodeFormatterEnvironment();
        xmlFile = File.createTempFile("test", ".xml");
        xmlFile.deleteOnExit();
    }

    @AfterAll
    static void tearDown() throws Exception{
        formatter.close();
    }

    private void givenXmlFile(String filename) throws Exception{
        Files.copy(this.getClass().getResourceAsStream("/testfiles/xml/given/" + filename), xmlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }


    public static String[] xmlTestFiles(){
        return new String[]{
                "students.xml"
        };
    }

    @ParameterizedTest
    @MethodSource("xmlTestFiles")
    void testReformatXmlDefaultOk(String filename) throws Exception {
        givenXmlFile(filename);
        formatter.format(new String[]{"-allowDefaults", xmlFile.toString()});
        //reload file
        assertXmlDefaultsSame(filename);
    }

    void assertXmlDefaultsSame(String xmlFilename){
        Path expected = new File(this.getClass().getResource("/testfiles/xml/expected/defaults/" + xmlFilename).getFile()).toPath();
        Assertions.assertThat(xmlFile.toPath()).hasSameBinaryContentAs(expected);
    }


}

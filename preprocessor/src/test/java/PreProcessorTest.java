import jdk.jfr.Description;
import org.example.PreProcessor;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PreProcessorTest {

    // Given A Regex Pattern Should Return Text Replaced By Spaces
    @Test
    public void cleanByRegex_givenARegexPatternShouldReturnTextReplacedBySpaces() {
        String input = "Replace this pattern: ABC123 with XYZ789.";
        String regexPattern = "[A-Z]{3}\\d{3}";
        String actualResult = new PreProcessor().cleanByRegex(input, regexPattern);

        String expectedResult = "Replace this pattern:   with  ";
        assertEquals(expectedResult, actualResult);
    }


    @Test
    public void testRegexReplacement_shouldChangePatternWithSpace() {
        // Input string
        String input = "Replace this pattern: ABC123 with XYZ789.";

        // Expected result after replacement
        String expectedResult = "Replace this pattern:   with  ";

        // Perform the replacement
        String actualResult = new PreProcessor().cleanText(input);

        // Assert the result
        assertEquals(expectedResult, actualResult);
    }
}
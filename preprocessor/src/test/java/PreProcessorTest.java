import jdk.jfr.Description;
import org.example.PreProcessor;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PreProcessorTest {

    // Given A Regex Pattern Should Return Text Replaced By Spaces
    @Test
    public void cleanByRegex() {
        String input = "Replace this pattern: ABC123 with XYZ789.";
        String regexPattern = "[A-Z]{3}\\d{3}";
        String actualResult = new PreProcessor().cleanByRegex(input, regexPattern, " ");

        String expectedResult = "Replace this pattern:   with  ";
        assertEquals(expectedResult, actualResult);
    }

    // Given A Text Should Return Text Without Html Tags
    @Test
    public void cleanHtmlTags() {
        String input = "Ciao <b>mondo</b>!";
        String actualResult = new PreProcessor().cleanHtmlTags(input);

        String expectedResult = "Ciao  mondo";
        assertEquals(expectedResult, actualResult);
    }

    // Given A Text Should Return Text Without Non Digit And Punctuation
    @Test
    public void cleanNonDigit() {
        String input = "Ciao, mondo!";
        String actualResult = new PreProcessor().cleanNonDigit(input);

        String expectedResult = "Ciao mondo";
        assertEquals(expectedResult, actualResult);
    }

    // Given A Text Should Return Text Without Consecutive Letters
    @Test
    public void cleanConsecutiveLetters() {
        String input = "Ciaooooo, monddddooooo!";
        String actualResult = new PreProcessor().cleanConsecutiveLetters(input);

        String expectedResult = "Ciaoo, monddoo!";
        assertEquals(expectedResult, actualResult);
    }

    // Given A Text Should Return Text Without Multiple Spaces
    @Test
    public void cleanMultipleSpaces() {
        String input = "Ciao    mondo!";
        String actualResult = new PreProcessor().cleanMultipleSpaces(input);

        String expectedResult = "Ciao mondo!";
        assertEquals(expectedResult, actualResult);
    }

    // Given A Text Should Return Text Without Urls
    @Test
    public void cleanUrl() {
        String input = "Ciao https://www.google.com/";
        String actualResult = new PreProcessor().cleanUrl(input);

        String expectedResult = "Ciao";
        assertEquals(expectedResult, actualResult);
    }
}
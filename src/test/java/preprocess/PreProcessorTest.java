package preprocess;

import it.unipi.dii.aide.mircv.preprocess.PreProcessor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PreProcessorTest {

    // Given A Regex Pattern Should Return Text Replaced By Spaces
    @Test
    public void cleanByRegex() {
        String input = "Replace this pattern: ABC123 with XYZ789.";
        String regexPattern = "[A-Z]{3}\\d{3}";
        String actualResult = PreProcessor.getInstance().cleanByRegex(input, regexPattern, " ");

        String expectedResult = "Replace this pattern:   with  .";
        assertEquals(expectedResult, actualResult);
    }

    // Given A Text Should Return Text Without Html Tags
    @Test
    public void cleanHtmlTags() {
        String input = "Ciao <b>mondo</b>!";
        String actualResult = PreProcessor.getInstance().cleanHtmlTags(input);

        String expectedResult = "Ciao  mondo !";
        assertEquals(expectedResult, actualResult);
    }

    // Given A Text Should Return Text Without Non Digit And Punctuation
    @Test
    public void cleanNonDigit() {
        String input = "Ciao, mondo!";
        String actualResult =  PreProcessor.getInstance().cleanNonDigit(input);

        String expectedResult = "Ciao  mondo";
        assertEquals(expectedResult, actualResult);
    }

    // Given A Text Should Return Text Without Consecutive Letters
    @Test
    public void cleanConsecutiveLetters() {
        String input = "Ciaooooo, monddddooooo!";
        String actualResult =  PreProcessor.getInstance().cleanConsecutiveLetters(input);

        String expectedResult = "Ciaoo, monddoo!";
        assertEquals(expectedResult, actualResult);
    }

    // Given A Text Should Return Text Without Multiple Spaces
    @Test
    public void cleanMultipleSpaces() {
        String input = "Ciao    mondo!";
        String actualResult = PreProcessor.getInstance().cleanMultipleSpaces(input);

        String expectedResult = "Ciao mondo!";
        assertEquals(expectedResult, actualResult);
    }

    // Given A Text Should Return Text Without Camel Case
    @Test
    public void replaceCamelCase() {
        String input = "CiaoMondo!";
        String actualResult =  PreProcessor.getInstance().addSpaceToCamelCase(input);

        String expectedResult = "Ciao Mondo!";
        assertEquals(expectedResult, actualResult);
    }

    // Given A Text Should Return Text Without Urls
    @Test
    public void cleanUrl() {
        String input = "Ciao https://www.google.com/";
        String actualResult = PreProcessor.getInstance().cleanUrl(input);

        String expectedResult = "Ciao";
        assertEquals(expectedResult, actualResult);
    }

    // Given A Text Should Return Text Without Html Tags, Non Digit And Punctuation, Consecutive Letters, Multiple Spaces, Camel Case And Urls
    @Test
    public void cleanText() {
        String input = "Ciao <b>mondo</b>! Ciao, mondo! Ciaooooo, monddddooooo! Ciao    mondo! CiaoMondo! Ciao https://www.google.com/";
        String actualResult = PreProcessor.getInstance().cleanText(input);

        String expectedResult = "Ciao mondo Ciao mondo Ciaoo monddoo Ciao mondo Ciao Mondo Ciao";
        assertEquals(expectedResult, actualResult);
    }
}
import org.example.PreProcessor;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PreProcessorTest {

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
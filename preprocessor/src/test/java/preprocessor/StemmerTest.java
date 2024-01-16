package preprocessor;

import it.unipi.dii.aide.mircv.preprocessor.Stemmer;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StemmerTest {

    // Given A Text array Should Return Text array without stopwords
    @Test
    public void removeStopwords() {
        String[] input = {"Hello", "World", "the", "and", "or"};
        String[] actualResult = Stemmer.getInstance().removeStopwords(input);

        String[] expectedResult = {"Hello", "World"};
        assertEquals(expectedResult[0], actualResult[0]);
        assertEquals(expectedResult[1], actualResult[1]);
    }

    // Given A Text array Should Return Text array with stemmed words (camel case)
    @Test
    public void getStems() {
        String[] input = {"Hello", "World"};
        String[] actualResult = Stemmer.getStems(input);

        String[] expectedResult = {"hello", "world"};
        assertEquals(expectedResult[0], actualResult[0]);
        assertEquals(expectedResult[1], actualResult[1]);
    }

    // Given A Text in camel case should Return Text with spaces between Camel Case
    @Test
    public void tokenize() {
        String input = "HelloWorld";
        String[] actualResult = Stemmer.getInstance().tokenize(input);

        String[] expectedResult = {"hello", "world"};
        assertEquals(expectedResult[0], actualResult[0]);
        assertEquals(expectedResult[1], actualResult[1]);
    }

    // Given document should Return processed document
    @Test
    public void processDocument() {
        String input = "If you hold to my teaching, you are really my disciples. Then you will know the truth, and the truth will set you free";
        String actualResult = Stemmer.getInstance().processDocument(input);

        String expectedResult = "hold teaching, disciples. will truth, truth will set free";
        assertEquals(expectedResult, actualResult);
    }
}

package preprocessor;

import it.unipi.dii.aide.mircv.preprocessor.Stopword;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StopwordTest {

    // Given A Text array Should Return Text array without stopwords
    @Test
    public void removeStopwords() {
        String[] input = {"Hello", "World", "the", "and", "or"};
        String[] actualResult = Stopword.getInstance().removeStopwords(input);

        String[] expectedResult = {"Hello", "World"};
        assertEquals(expectedResult[0], actualResult[0]);
        assertEquals(expectedResult[1], actualResult[1]);
    }

    // Given A Text array Should Return Text array with stemmed words (camel case)
    @Test
    public void getStems() {
        String[] input = {"Hello", "World"};
        String[] actualResult = Stopword.getStems(input);

        String[] expectedResult = {"hello", "world"};
        assertEquals(expectedResult[0], actualResult[0]);
        assertEquals(expectedResult[1], actualResult[1]);
    }
}

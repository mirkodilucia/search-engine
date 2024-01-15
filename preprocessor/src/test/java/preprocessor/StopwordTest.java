package preprocessor;

import it.unipi.dii.aide.mircv.preprocessor.Stopword;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StopwordTest {

    @Test
    public void removeStopwords() {
        String[] input = {"Hello", "World"};
        String[] actualResult = Stopword.getInstance().removeStopwords(input);

        String[] expectedResult = {"Hello", "World"};
        assertEquals(expectedResult[0], actualResult[0]);
        assertEquals(expectedResult[1], actualResult[1]);
    }

}

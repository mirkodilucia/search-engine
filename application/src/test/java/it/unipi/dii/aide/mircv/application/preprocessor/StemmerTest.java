package it.unipi.dii.aide.mircv.application.preprocessor;

import it.unipi.dii.aide.mircv.application.config.Config;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StemmerTest {

    static Config setup() {
        Config config = new Config();
        config.setRemoveStopword(true);
        config.setStopwordsPath("../resources/stopwords.dat");
        return config;
    }

    // Given A Text array Should Return Text array without stopwords
    @Test
    public void removeStopwords() {
        Config config = setup();

        String[] input = {"Hello", "World", "the", "and", "or"};
        String[] actualResult = Stemmer.with(config).removeStopwords(input);

        String[] expectedResult = {"Hello", "World"};
        assertEquals(expectedResult[0], actualResult[0]);
        assertEquals(expectedResult[1], actualResult[1]);
    }

    // Given A Text array Should Return Text array with stemmed words (camel case)
    @Test
    public void getStems() {
        Config config = setup();

        String[] input = {"Hello", "World"};
        String[] actualResult = Stemmer.with(config).getStems(input);

        String[] expectedResult = {"hello", "world"};
        assertEquals(expectedResult[0], actualResult[0]);
        assertEquals(expectedResult[1], actualResult[1]);
    }

    // Given A Text in camel case should Return Text with spaces between Camel Case
    @Test
    public void tokenize() {
        Config config = setup();

        String input = "HelloWorld";
        String[] actualResult = Stemmer.with(config).tokenize(input);

        String[] expectedResult = {"hello", "world"};
        assertEquals(expectedResult[0], actualResult[0]);
        assertEquals(expectedResult[1], actualResult[1]);
    }

    // Given document should Return processed document
    @Test
    public void processDocument() {
        Config config = setup();

        String input = "If you hold to my teaching, you are really my disciples. Then you will know the truth, and the truth will set you free";
        String actualResult = Stemmer.with(config).processDocument(input);

        String expectedResult = "hold teaching, disciples. will truth, truth will set free";
        assertEquals(expectedResult, actualResult);
    }
}

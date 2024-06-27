package indexer.spimi.model;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.indexer.model.Vocabulary;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class VocabularyTest {

    @Test
    public void testAddTermVocabulary() {
        Config config = new Config();

        Vocabulary vocabulary = new Vocabulary(config);

        List<String> terms = Arrays.asList("test", "test2", "test3");
        terms.forEach(term -> vocabulary.addTerm(term, 1));

        assert !vocabulary.getIndex().isEmpty();
    }

    @Test
    public void testWriteVocabulary() {
        Config config = new Config();

        Vocabulary vocabulary = new Vocabulary(config);

        List<String> terms = Arrays.asList("test", "test2", "test3");
        terms.forEach(term -> vocabulary.addTerm(term, 1));

        assert !vocabulary.getIndex().isEmpty();
    }

}

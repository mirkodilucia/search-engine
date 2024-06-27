package indexer.spimi;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.indexer.model.Vocabulary;
import it.unipi.dii.aide.mircv.indexer.spimi.Spimi;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpimiTest {

    @Test
    public void testSpimi() {
        Config config = new Config();
        Spimi spimi = Spimi.with(config);
        int spimiIndex = spimi.executeSpimi();
        assertTrue(spimiIndex > 0);

        Vocabulary vocabulary = new Vocabulary(config);
        assertNotNull(vocabulary);

        List<String> terms = Arrays.asList("test", "test2", "test3");
        terms.forEach(term -> vocabulary.addTerm(term, 1));


        assertTrue(vocabulary.getIndex().size() > 0);
    }
}

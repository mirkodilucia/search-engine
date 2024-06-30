package indexer.merger;

import java.io.IOException;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.config.InvertedIndexConfig;
import it.unipi.dii.aide.mircv.config.PartialResultsConfig;
import it.unipi.dii.aide.mircv.indexer.merger.Merger;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class MergerTest {

    static Config config;

    @BeforeAll
    public static void setup() {
        config = new Config();
        config.setVocabularyPath(
                "data_test/mergerWorkerTest/vocabulary"
        ).setPartialResultConfig(
                new PartialResultsConfig(
                        "data_test/mergerWorkerTest/partial_results",
                        "data_test/mergerWorkerTest/partial_results",
                        "data_test/mergerWorkerTest/partial_results")
        ).setInvertedIndexConfig(
                new InvertedIndexConfig(
                        "data_test/mergerWorkerTest/indexes_docs",
                        "data_test/mergerWorkerTest/indexes_freqs")
        );
    }

    @Test
    public void initializeMerger() {
        Merger merger = Merger.with(config);
        assert(merger != null);
    }

    @Test
    public void singleIndexMergeWithoutCompression() {
        MergerWithoutCompression.mergeSingleIndex(config);
    }

    /*
    @Test
    public void initializeMergerWithConfig() {
        Merger merger = Merger.with(config);

        VocabularyEntry vocabularyEntry = merger.getNextTerms(0);
        assert(vocabularyEntry.getTerm().compareTo("alberobello") == 0);
    }


    @Test
    public void mergerGetMinimumTerm() {
        Merger merger = Merger.with(config);
        merger.mergeIndexes(3);

        String vocabularyEntry = merger.getMinimumTerm();
        assert(vocabularyEntry.compareTo("alberobello") == 0);
    }
    */

    @Test
    public void mergeIndexes() {
        Merger merger = Merger.with(config);
        boolean result = merger.mergeIndexes(3);

        assert(result);
    }
}
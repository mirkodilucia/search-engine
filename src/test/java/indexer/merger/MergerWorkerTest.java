package indexer.merger;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.config.InvertedIndexConfig;
import it.unipi.dii.aide.mircv.config.PartialResultsConfig;
import it.unipi.dii.aide.mircv.config.VocabularyConfig;
import it.unipi.dii.aide.mircv.indexer.merger.MergerWorker;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MergerWorkerTest {

    static Config config;
    private static Vocabulary vocabulary;

    @BeforeAll
    public static void setup() {
        config = new Config();
        config.setVocabularyPath(
                new VocabularyConfig(
                "data_test/mergerWorkerTest/vocabulary",
                "data_test/mergerWorkerTest/frequencies.dat",
                "data_test/mergerWorkerTest/doc_ids.dat",
                "data_test/mergerWorkerTest/vocabulary"
                ))
            .setPartialResultConfig(new PartialResultsConfig(
                    "data_test/mergerWorkerTest/partial_results",
                    "data_test/mergerWorkerTest/partial_results",
                    "data_test/mergerWorkerTest/partial_results"
            )).setPartialIndexConfig(new InvertedIndexConfig(
                "data_test/mergerWorkerTest/indexes_docs",
                "data_test/mergerWorkerTest/indexes_freqs"));

        vocabulary = Vocabulary.with(config);
    }

    @Test
    public void testGetMinimumTerm() {
        try {
            VocabularyEntry[] nextTerms = new VocabularyEntry[1];
            nextTerms[0] = new VocabularyEntry("term");
            int numIndexes = 1;

            vocabulary.put("term", nextTerms[0]);

            MergerWorker mergerWorker = MergerWorker.with(config, numIndexes);
            mergerWorker.processTerm(nextTerms[0], nextTerms[0].getTerm());

            String result = mergerWorker.getMinumumTerm();

            assert result.equals("term");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetMinimumTermWithMultipleTerm() {
        try {
            VocabularyEntry[] nextTerms = new VocabularyEntry[2];
            nextTerms[0] = new VocabularyEntry("Apple");
            nextTerms[1] = new VocabularyEntry("apple");
            int numIndexes = 1;
            MergerWorker mergerWorker = MergerWorker.with(config, numIndexes);

            vocabulary.put("Apple", nextTerms[0]);
            vocabulary.put("apple", nextTerms[1]);

            for (int i = 0; i < numIndexes; i++) {
                mergerWorker.processTerm(nextTerms[i], nextTerms[i].getTerm());
            }

            String result = mergerWorker.getMinumumTerm();
            assert result.equals("Apple");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    @Test
    public void processTermTest() {
        VocabularyEntry[] nextTerms = new VocabularyEntry[1];
        nextTerms[0] = new VocabularyEntry("term", "../test/data/processTermTest");

        MergerWorker mergerWorker = MergerWorker.with(config, 1);

        VocabularyEntry vocabularyEntry = new VocabularyEntry("term", "../test/data/processTermTest");
        PostingList result = null;

        try {
            MergerLoader mergerLoader = MergerLoaderMock.load(config);
            result = mergerWorker.processTerm(mergerLoader, vocabularyEntry, "term");

            assert result.getTerm().equals("term");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     */

}

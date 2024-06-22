package it.unipi.dii.aide.mircv.application.indexer.merger;

import it.unipi.dii.aide.mircv.application.ConfigUtils;
import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.VocabularyEntry;
import it.unipi.dii.aide.mircv.application.data.PostingList;
import it.unipi.dii.aide.mircv.application.indexer.MergerLoader;
import it.unipi.dii.aide.mircv.application.indexer.MergerWorker;
import org.junit.Test;

public class MergerWorkerTest
{
    @Test
    public void testGetMinimumTerm() {
        Config config = ConfigUtils.getConfig();

        VocabularyEntry[] nextTerms = new VocabularyEntry[1];
        nextTerms[0] = new VocabularyEntry("term", "../test/data/getMinimumTermTest");
        int numIndexes = 1;
        MergerWorker mergerWorker = MergerWorker.with(config, numIndexes, nextTerms);
        String result = mergerWorker.getMinimumTerm();
        assert result.equals("term");
    }

    @Test
    public void testGetMinimumTermWithMultipleTerm() {
        Config config = ConfigUtils.getConfig();

        VocabularyEntry[] nextTerms = new VocabularyEntry[2];
        nextTerms[0] = new VocabularyEntry("Apple", "../test/data/getMinimumTermTest");
        nextTerms[1] = new VocabularyEntry("apple", "../test/data/getMinimumTermTest");
        int numIndexes = 1;
        MergerWorker mergerWorker = MergerWorker.with(config, numIndexes, nextTerms);
        String result = mergerWorker.getMinimumTerm();
        assert result.equals("Apple");
    }

    @Test
    public void processTermTest() {
        Config config = ConfigUtils.getConfig();

        VocabularyEntry[] nextTerms = new VocabularyEntry[1];
        nextTerms[0] = new VocabularyEntry("term", "../test/data/processTermTest");

        MergerWorker mergerWorker = MergerWorker.with(config, 1, nextTerms);

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

}

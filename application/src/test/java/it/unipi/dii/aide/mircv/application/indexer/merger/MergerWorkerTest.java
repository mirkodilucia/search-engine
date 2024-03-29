package it.unipi.dii.aide.mircv.application.indexer.merger;

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
        VocabularyEntry[] nextTerms = new VocabularyEntry[1];
        nextTerms[0] = new VocabularyEntry("term", "../test/data/getMinimumTermTest");
        int numIndexes = 1;
        Config config = new Config();
        MergerWorker mergerWorker = MergerWorker.with(config);
        String result = mergerWorker.getMinimumTerm(nextTerms, numIndexes);
        assert result.equals("term");
    }

    @Test
    public void testGetMinimumTermWithMultipleTerm() {
        VocabularyEntry[] nextTerms = new VocabularyEntry[2];
        nextTerms[0] = new VocabularyEntry("Apple", "../test/data/getMinimumTermTest");
        nextTerms[1] = new VocabularyEntry("apple", "../test/data/getMinimumTermTest");
        int numIndexes = 1;
        Config config = new Config();
        MergerWorker mergerWorker = MergerWorker.with(config);
        String result = mergerWorker.getMinimumTerm(nextTerms, numIndexes);
        assert result.equals("Apple");
    }

    @Test
    public void processTermTest() {
        VocabularyEntry[] nextTerms = new VocabularyEntry[1];
        nextTerms[0] = new VocabularyEntry("term", "../test/data/processTermTest");

        Config config = new Config();
        config.setStopwordsPath("../resources/stopwords.dat");
        config.setPathToInvertedIndexDocs("../test/data/processTerm/invertedIndexDocsTest");
        config.setPathToInvertedIndexFreq("../test/data/processTerm/invertedIndexFreqTest");
        config.setPathToBlockDescriptors("../test/data/processTerm/blockDescriptorsTest");

        MergerWorker mergerWorker = MergerWorker.with(config);

        VocabularyEntry vocabularyEntry = new VocabularyEntry("term", "../test/data/processTermTest");
        PostingList result = null;

        try {
            MergerLoader mergerLoader = MergerLoaderMock.load(config);
            result = mergerWorker.processTerm(mergerLoader, nextTerms, vocabularyEntry, "term");

            assert result.getTerm().equals("term");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

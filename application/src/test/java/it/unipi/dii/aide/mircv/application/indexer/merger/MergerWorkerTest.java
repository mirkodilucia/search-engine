package it.unipi.dii.aide.mircv.application.indexer.merger;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.VocabularyEntry;
import it.unipi.dii.aide.mircv.application.data.PostingList;
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



}

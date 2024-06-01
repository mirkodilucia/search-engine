package it.unipi.dii.aide.mircv.application.indexer.merger;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.VocabularyEntry;
import org.junit.Test;

public class Merger3Test {

    @Test
    public void initializeMerger() {
        Config config = new Config();
        config.setDocumentIdFolder("../test/data/initializeMerger/documentIdTest");
        config.setDocumentFreqPath("../test/data/initializeMerger", "documentFreqsTest");
        config.setPathToVocabulary("../test/data/initializeMerger");
        config.setPartialIndexesPath("../test/data/initializeMerger");

        Merger3 merger = Merger3.with(config, 1);

        assert(merger != null);
    }

    @Test
    public void initializeMergerWithConfig() {
        Config config = new Config();
        config.setDocumentIdFolder("../test/data/initializeMergerWithConfig/documentIdTest");
        config.setDocumentFreqPath("../test/data/initializeMergerWithConfig", "documentFreqsTest");
        config.setPathToVocabulary("../test/data/initializeMergerWithConfig");
        config.setPartialIndexesPath("../test/data/initializeMergerWithConfig");
        Merger3 merger = Merger3.with(config, 1);

        VocabularyEntry vocabularyEntry = merger.getNextTerms(0);
        assert(vocabularyEntry.getTerm().compareTo("alberobello") == 0);
    }


    @Test
    public void mergerGetMinimumTerm() {
        Config config = new Config();
        config.setDocumentIdFolder("../test/data/mergerGetMinimumTerm/documentIdTest");
        config.setDocumentFreqPath("../test/data/mergerGetMinimumTerm", "documentFreqsTest");
        config.setPathToVocabulary("../test/data/mergerGetMinimumTerm/vocabulary");
        config.setPartialIndexesPath("../test/data/mergerGetMinimumTerm");

        Merger3 merger = Merger3.with(config, 3);
        String vocabularyEntry = merger.getMinimumTerm();
        assert(vocabularyEntry.compareTo("alberobello") == 0);
    }

    @Test
    public void mergeIndexes() {
        Config config = new Config();

        config.setPathToInvertedIndexDocs("../test/data/merger/mergeIndexes/documents");
        config.setPathToInvertedIndexFreq("../test/data/merger/mergeIndexes/frequency");
        config.setPartialVocabularyDir("../test/data/merger/mergeIndexes/vocabulary");

        config.setDocumentIdFolder("../test/data/merger/mergeIndexes/documentIdTest");
        config.setPathToVocabulary("../test/data/merger/mergeIndexes/vocabulary_test");

        config.setPartialIndexesPath("../test/data/merger/mergeIndexes/partialIndexes");
        config.setPathToBlockDescriptors("../test/data/merger/mergeIndexes/blockDescriptors");

        Merger3 merger = Merger3.with(config, 3);
        boolean result = merger.mergeIndexes(false, false);

        assert(result);
    }
}

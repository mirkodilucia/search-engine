package it.unipi.dii.aide.mircv.application.indexer.merger;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.indexer.Merger2;
import it.unipi.dii.aide.mircv.application.indexer.MergerLoader;
import org.junit.Test;

public class Merger2Test {



    @Test
    public void mergeIndexesTest() {
        Config config = new Config();
        config.setStopwordsPath("../resources/stopwords.dat");
        config.setDocumentFreqPath("../test/data/mergeIndex", "documentFreqsTest");
        config.setPathToInvertedIndexDocs("../test/data/mergeIndex/invertedIndexDocsTest");
        config.setPathToInvertedIndexFreq("../test/data/mergeIndex/invertedIndexFreqTest");
        config.setPathToBlockDescriptors("../test/data/mergeIndex/blockDescriptorsTest");
        config.setPartialIndexesPath("../test/data/mergeIndex/mergeIndexesTest");
        config.setPathToVocabulary("../test/data/mergeIndex/mergeIndexesTest");
        config.setCollectionStatisticsPath("../test/data/mergeIndex/mergeIndexesTest");

        Merger2 merger = Merger2.with(config, 0);

        try {
            merger.mergeIndexes(1, false, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void singleIndexMergeWithoutCompression() {
        Config config = new Config();
        config.setStopwordsPath("../resources/stopwords.dat");
        config.setDocumentIndexPath("../test/data/singleIndexMergeWithoutCompression/documentIndexTest");
        config.setDocumentFreqPath("../test/data/singleIndexMergeWithoutCompression", "documentFreqsTest");
        config.setPathToInvertedIndexDocs("../test/data/singleIndexMergeWithoutCompression/invertedIndexDocsTest");
        config.setPathToInvertedIndexFreq("../test/data/singleIndexMergeWithoutCompression/invertedIndexFreqTest");
        config.setPathToBlockDescriptors("../test/data/singleIndexMergeWithoutCompression/blockDescriptorsTest");
        config.setPartialIndexesPath("../test/data/singleIndexMergeWithoutCompression/mergeIndexesTest");
        config.setDocumentIdFolder("../test/data/singleIndexMergeWithoutCompression/documentIdTest");
        config.setDocumentFreqPath("../test/data/singleIndexMergeWithoutCompression", "documentFreqsTest");
        config.setPathToVocabulary("../test/data/singleIndexMergeWithoutCompression/mergeIndexesTest/vocabulary_0");
        config.setCollectionStatisticsPath("../test/data/singleIndexMergeWithoutCompression/mergeIndexesTest/statistics");
        config.setCompression(false);

        MergerWithouCompression.mergeSingleIndex(config, config.isCompressionEnabled());
    }
}

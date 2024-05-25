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

        Merger2 merger = Merger2.with(config, 1);

        try {
            merger.mergeIndexes(1, false, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

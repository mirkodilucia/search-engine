package it.unipi.dii.aide.mircv.application.indexer.merger;

import it.unipi.dii.aide.mircv.application.ConfigUtils;
import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.indexer.Merger2;
import it.unipi.dii.aide.mircv.application.indexer.MergerLoader;
import org.junit.Test;

public class Merger2Test {

    @Test
    public void mergeIndexesTest() {
        Config config = ConfigUtils.getConfig("merger2Test");
        Merger2 merger = Merger2.with(config, 1);

        try {
            merger.mergeIndexes(1, config.isCompressionEnabled(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void singleIndexMergeWithoutCompression() {
        Config config = ConfigUtils.getConfig("merger2Test");

        MergerWithouCompression.mergeSingleIndex(config, config.isCompressionEnabled());
    }
}

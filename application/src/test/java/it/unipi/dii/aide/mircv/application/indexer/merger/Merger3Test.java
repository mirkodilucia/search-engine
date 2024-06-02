package it.unipi.dii.aide.mircv.application.indexer.merger;

import it.unipi.dii.aide.mircv.application.ConfigUtils;
import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.VocabularyEntry;
import org.junit.Test;

public class Merger3Test {

    @Test
    public void initializeMerger() {
        Config config = ConfigUtils.getConfig("merger3Test");
        Merger3 merger = Merger3.with(config, 1);

        assert(merger != null);
    }

    @Test
    public void initializeMergerWithConfig() {
        Config config = ConfigUtils.getConfig("merger3Test");

        Merger3 merger = Merger3.with(config, 1);

        VocabularyEntry vocabularyEntry = merger.getNextTerms(0);
        assert(vocabularyEntry.getTerm().compareTo("alberobello") == 0);
    }


    @Test
    public void mergerGetMinimumTerm() {
        Config config = ConfigUtils.getConfig("merger3Test");

        Merger3 merger = Merger3.with(config, 3);
        String vocabularyEntry = merger.getMinimumTerm();
        assert(vocabularyEntry.compareTo("alberobello") == 0);
    }

    @Test
    public void mergeIndexes() {
        Config config = ConfigUtils.getConfig("merger3Test");

        Merger3 merger = Merger3.with(config, 3);
        boolean result = merger.mergeIndexes(false, false);

        assert(result);
    }
}

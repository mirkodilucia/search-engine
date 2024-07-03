package indexer.merger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unipi.dii.aide.mircv.config.*;
import it.unipi.dii.aide.mircv.indexer.merger.Merger;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.BaseVocabularyEntry;
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
                new VocabularyConfig(
                        "data_test/mergerWorkerTest/vocabulary.dat")
        ).setBlockDescriptorPath(
                new BlockDescriptorConfig(
                "data_test/mergerWorkerTest/block_descriptors.dat", false
                )).setPartialResultConfig(
                new PartialResultsConfig(
                        "data_test/mergerWorkerTest/partial_results",
                        "data_test/mergerWorkerTest/partial_results",
                        "data_test/mergerWorkerTest/partial_results")
        ).setInvertedIndexConfig(
                new InvertedIndexConfig(
                        "data_test/mergerWorkerTest/indexes_freqs.dat",
                        "data_test/mergerWorkerTest/indexes_docs.dat"
                        )
        );
    }

    @Test
    public void initializeMerger() {
        Merger merger = Merger.with(config);
        assert(merger != null);
    }

    @Test
    public void singleIndexMergeWithoutCompression() {
        createVocabulary();
        MergerWithoutCompression.mergeSingleIndex(config);
    }

    private void createVocabulary() {
        Vocabulary vocabulary = Vocabulary.with(config);

        ArrayList<VocabularyEntry> vocabularyEntries = new ArrayList<>(List.of(new VocabularyEntry[]{
                new VocabularyEntry("alberobello",
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                1, 1, 1, 1),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                0, 0, 0, 0, 0, 0
                        )
                ),
                new VocabularyEntry("roma",
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                1, 1, 1, 1),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                0, 0, 0, 0, 0, 0
                        )
                ),
                new VocabularyEntry("praga",
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                1, 1, 1, 1),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                0, 0, 0, 0, 0, 0
                        )
                ),
                new VocabularyEntry("parigi",
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                1, 1, 1, 1),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                0, 0, 0, 0, 0, 0
                        )
                ),
                new VocabularyEntry("berlino",
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                1, 1, 1, 1),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                0, 0, 0, 0, 0, 0
                        )
                ),
                new VocabularyEntry("tokyo",
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                1, 1, 1, 1),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                0, 0, 0, 0, 0, 0
                        )
                ),
                new VocabularyEntry("zurigo",
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                1, 1, 1, 1),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                0, 0, 0, 0, 0, 0
                        )
                )
        }));

        long offset = 0;
        for (VocabularyEntry vocabularyEntry : vocabularyEntries) {
            vocabularyEntry.writeEntry(offset, vocabulary.getVocabularyChannel());
            vocabulary.put(vocabularyEntry.getTerm(), vocabularyEntry);
            offset += VocabularyEntry.ENTRY_SIZE;
        }
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
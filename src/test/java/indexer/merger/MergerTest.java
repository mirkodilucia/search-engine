package indexer.merger;

import java.util.ArrayList;
import java.util.List;

import it.unipi.dii.aide.mircv.config.*;
import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.indexer.merger.Merger;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.BaseVocabularyEntry;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MergerTest {

    static Config config;

    @BeforeAll
    public static void setup() {
        config = new Config(
                "data_test/mergerWorkerTest/testDir",
                "data_test/mergerWorkerTest/debugDir",
                true
        );
        config.setVocabularyPath(
                new VocabularyConfig(
                        "data_test/mergerWorkerTest/vocabulary.dat",
                        "data_test/mergerWorkerTest/documentIndexState.dat"
                )).setBlockDescriptorPath(
                new BlockDescriptorConfig(
                "data_test/mergerWorkerTest/block_descriptors.dat", false
                )).setPartialResultConfig(
                new PartialResultsConfig(
                        "data_test/mergerWorkerTest/partial_results",
                        "data_test/mergerWorkerTest/partial_results",
                        "data_test/mergerWorkerTest/partial_results"
                )).setInvertedIndexConfig(
                new InvertedIndexConfig(
                        "data_test/mergerWorkerTest/inverted_indexes_freqs.dat",
                        "data_test/mergerWorkerTest/inverted_indexes_docs.dat"
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
        DocumentIndexState.with(config);
        Vocabulary vocabulary = Vocabulary.with(config);

        ArrayList<VocabularyEntry> vocabularyEntries = new ArrayList<>(List.of(new VocabularyEntry[]{
                new VocabularyEntry("alberobello", 2,
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                1, 1, 1, 1),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                0, 0, 0, 0, 1, 0
                        )
                ),
                new VocabularyEntry("amburgo", 3,
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                1, 1, 1, 1),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                8, 0, 8, 0, 1, 32
                        )
                ),
                new VocabularyEntry("pisa", 3,
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                1, 1, 1, 1),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                20, 0, 20, 0, 1, 64
                        )
                ),
                new VocabularyEntry("zurigo", 2,
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                1, 1, 1, 1),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                32, 0, 32, 0, 1, 96
                        )
                )
        }));

        long offset = 0;
        for (VocabularyEntry vocabularyEntry : vocabularyEntries) {
            vocabularyEntry.writeEntry(offset, vocabulary.getVocabularyChannel());
            vocabulary.put(vocabularyEntry.getTerm(), vocabularyEntry);
            offset += VocabularyEntry.ENTRY_SIZE;
        }

        DocumentIndexState.updateVocabularySize(vocabularyEntries.size());
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
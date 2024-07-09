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
                "data_test/mergerWorkerTest/documentIndex",
                "data_test/mergerTest/testDir",
                "data_test/mergerTest/debugDir",
                true
        );
        config.setVocabularyPath(
                new VocabularyConfig(
                        "data_test/mergerTest/vocabulary.dat",
                        "data_test/mergerTest/documentIndexState.dat"
                )).setBlockDescriptorPath(
                new BlockDescriptorConfig(
                "data_test/mergerTest/block_descriptors.dat", false
                )).setPartialResultConfig(
                new PartialResultsConfig(
                        "data_test/mergerTest/partial_results",
                        "data_test/mergerTest/partial_results",
                        "data_test/mergerTest/partial_results"
                )).setInvertedIndexConfig(
                new InvertedIndexConfig(
                        "data_test/mergerTest/inverted_indexes_freqs.dat",
                        "data_test/mergerTest/inverted_indexes_docs.dat"
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
                new VocabularyEntry("alberobello", 2, 0.3979400086720376, 3,
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                1, 3, 0.5878056449127935, 0.3288142794660968),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                0, 0, 0, 0, 1, 0
                        )
                ),
                new VocabularyEntry("amburgo", 3, 0.22184874961635637, 5,
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                1, 3, 0.3769143710976413, 0.18331164287548693),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                8, 0, 8, 0, 1, 32
                        )
                ),
                new VocabularyEntry("pisa", 3, 0.22184874961635637, 2,
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                3, 2, 0.2886318777514278, 0.1412129473145704),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                20, 0, 20, 0, 1, 64
                        )
                ),
                new VocabularyEntry("zurigo", 2, 0.3979400086720376, 2,
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                                4, 1, 0.5177318877571058, 0.16596550124710574),
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
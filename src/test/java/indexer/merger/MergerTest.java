package indexer.merger;

import java.util.ArrayList;
import java.util.List;

import it.unipi.dii.aide.mircv.config.model.*;
import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.indexer.merger.Merger;
import it.unipi.dii.aide.mircv.indexer.model.BlockDescriptor;
import it.unipi.dii.aide.mircv.indexer.model.Posting;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.BaseVocabularyEntry;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import it.unipi.dii.aide.mircv.utils.FileHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static indexer.merger.MergerWithoutCompression.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MergerTest {

    static Config config;

    @BeforeAll
    public static void setup() {
        config = new Config(
                "data_test/mergerWorkerTest/documentIndex",
                "data/collection.tsv",
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

    private Vocabulary createVocabulary() {
        Vocabulary vocabulary = Vocabulary.with(config);

        ArrayList<VocabularyEntry> vocabularyEntries = new ArrayList<>(List.of(new VocabularyEntry[]{
                new VocabularyEntry("alberobello", 2, 0.3979400086720376,
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(3,
                                1, 3, 0.5878056449127935, 0.3288142794660968),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                0, 0, 0, 0, 1, 0
                        )
                ),
                new VocabularyEntry("amburgo", 3, 0.22184874961635637,
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(5,
                                1, 3, 0.3769143710976413, 0.18331164287548693),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                8, 0, 8, 0, 1, 32
                        )
                ),
                new VocabularyEntry("pisa", 3, 0.22184874961635637,
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(2,
                                3, 2, 0.2886318777514278, 0.1412129473145704),
                        new BaseVocabularyEntry.VocabularyMemoryInfo(
                                20, 0, 20, 0, 1, 64
                        )
                ),
                new VocabularyEntry("zurigo", 2, 0.3979400086720376,
                        new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(2,
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

        return vocabulary;
    }

    @BeforeEach
    void setUp() {
        //FileUtils.createDirectory(TEST_DIRECTORY);
        //createDirectory(TEST_DIRECTORY+"/partial_freqs");
        //createDirectory(TEST_DIRECTORY+"/partial_docids");
        //createDirectory(TEST_DIRECTORY+"/partial_vocabulary");

        FileHandler.deleteFile(config.getBlockDescriptorsPath());
        FileHandler.deleteFile(config.getVocabularyPath());
        FileHandler.deleteFile(config.getInvertedIndexDocs());
        FileHandler.deleteFile(config.getInvertedIndexFreqsFile());
        FileHandler.deleteFile(config.getDocumentIndexFile());

        BlockDescriptor.setMemoryOffset(0);
        Vocabulary.with(config).unset();
        Merger.with(config).unset();
    }

    @Test
    public void initializeMerger() {
        Merger merger = Merger.with(config);
        assert(merger != null);
    }

    @Test
    public void singleIndexMergeWithoutCompression() {
        config.setScorerConfig(false, false, true);
        MergerWithoutCompression.mergeSingleIndex(config);
    }

    @Test
    void singleIndexMergeWithCompression() {
        config.setScorerConfig(false, true, true);
        MergerWithoutCompression.mergeSingleIndex(config);
    }

    @Test
    public void mergeSingleIndex() {
        Merger merger = Merger.with(config);
        boolean result = merger.mergeIndexes(3);

        assert(result);
    }

    public void mergeTwoIndexes(boolean vocabularyTest) {
        MergerWithoutCompression.mergeTwoIndexes(config);

        if(vocabularyTest){
            Vocabulary expectedVocabulary = createVocabulary();
            expectedVocabulary.readFromDisk();

            ArrayList<VocabularyEntry> retrievedVocabulary = new ArrayList<>();
            retrievedVocabulary.addAll(expectedVocabulary.values());

            assertArrayEquals(expectedVocabulary.getVocabularyEntries(), retrievedVocabulary.toArray(), "Vocabulary after merging is different from the expected vocabulary.");
        }else{
            ArrayList<ArrayList<Posting>> mergedLists = retrieveIndexFromDisk(config);
            ArrayList<ArrayList<Posting>> expectedResults = getPostingsResultForTwoIndex();
            assertEquals(expectedResults.toString(), mergedLists.toString(), "Error, expected results are different from actual results.");
        }
    }

    @Test
    void twoIndexesMergeWithoutCompression() {
        config.setScorerConfig(false, false, true);
        mergeTwoIndexes(false);
    }

    @Test
    void twoIndexesMergeWithCompression() {
        config.setScorerConfig(false, true, true);
        mergeTwoIndexes(false);
    }

    @Test
    void vocabularyTest(){
        config.setScorerConfig(false, false, true);
        mergeTwoIndexes(true);
    }

    @Test
    void vocabularyTest2() {
        config.setScorerConfig(false, true, true);
        mergeTwoIndexes(true);
    }
}
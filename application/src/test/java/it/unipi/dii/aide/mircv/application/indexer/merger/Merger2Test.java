package it.unipi.dii.aide.mircv.application.indexer.merger;

import it.unipi.dii.aide.mircv.application.ConfigUtils;
import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.BlockDescriptor;
import it.unipi.dii.aide.mircv.application.data.DocumentCollectionSize;
import it.unipi.dii.aide.mircv.application.data.Vocabulary;
import it.unipi.dii.aide.mircv.application.data.VocabularyEntry;
import it.unipi.dii.aide.mircv.application.indexer.Merger2;
import it.unipi.dii.aide.mircv.application.indexer.MergerLoader;
import it.unipi.dii.aide.mircv.application.utils.FileUtils;
import org.junit.Test;

public class Merger2Test {

    private static final String TEST_DIRECTORY = "../test/data/merger";
    private static final String PATH_TO_PARTIAL_VOCABULARY = TEST_DIRECTORY + "/partial_vocabulary/partial_vocabulary";
    private static final String PATH_TO_PARTIAL_FREQUENCIES = TEST_DIRECTORY+"/partial_freqs/partial_freqs";
    private static final String PATH_TO_PARTIAL_INDEXES_DOCS = TEST_DIRECTORY+"/partial_docids/partial_docids";
    private static final String DOCINDEX_PATH = TEST_DIRECTORY+"/docIndex";
    private static final String VOCABULARY_PATH = TEST_DIRECTORY+"/vocabulary";
    private static final String INVERTED_INDEX_DOCIDS = TEST_DIRECTORY+"/docids";
    private static final String INVERTED_INDEX_FREQS = TEST_DIRECTORY+"/freqs";
    private static final String BLOCK_DESCRIPTOR_PATH = TEST_DIRECTORY + "/block_descriptors";
    private static final String COLLECTION_STATISTICS_PATH = TEST_DIRECTORY + "/collection_statistics";


    static void setPaths(){
        //FileUtils.deleteFolder(TEST_DIRECTORY);
        FileUtils.createFolder(TEST_DIRECTORY);
        Merger2.setPathToVocabulary(VOCABULARY_PATH);
        Merger2.setPathToInvertedIndexDocs(INVERTED_INDEX_DOCIDS);
        Merger2.setPathToInvertedIndexFreqs(INVERTED_INDEX_FREQS);
        Merger2.setPathToBlockDescriptors(BLOCK_DESCRIPTOR_PATH);
        Merger2.setPathToPartialIndexesDocs(PATH_TO_PARTIAL_INDEXES_DOCS);
        Merger2.setPathToPartialIndexesFreqs(PATH_TO_PARTIAL_FREQUENCIES);
        Merger2.setPathToPartialVocabularies(PATH_TO_PARTIAL_VOCABULARY);
        VocabularyEntry.setBlockDescriptorsPath(BLOCK_DESCRIPTOR_PATH);
        BlockDescriptor.setInvertedIndexDocs(INVERTED_INDEX_DOCIDS);
        BlockDescriptor.setInvertedIndexFreqs(INVERTED_INDEX_FREQS);
        DocumentCollectionSize.setCollectionStatisticsPath(COLLECTION_STATISTICS_PATH);
        Vocabulary.setVocabularyPath(VOCABULARY_PATH);
        //if(Flags.isStemStopRemovalEnabled())
        //Preprocesser.readStopwords();
    }



    static void setUp() {
        //create directories to store partial frequencies, docids and vocabularies
        //FileUtils.createFolder(TEST_DIRECTORY);
        FileUtils.createFolder(TEST_DIRECTORY+"/partial_freqs");
        FileUtils.createFolder(TEST_DIRECTORY+"/partial_docids");
        FileUtils.createFolder(TEST_DIRECTORY+"/partial_vocabulary");
        BlockDescriptor.setMemoryOffset(0);
        Vocabulary.unsetInstance();
    }


    static void tearDown() {
        //delete directories to store partial frequencies, docids and vocabularies
        FileUtils.deleteFolder(TEST_DIRECTORY+"/partial_freqs");
        FileUtils.deleteFolder(TEST_DIRECTORY+"/partial_docids");
        FileUtils.deleteFolder(TEST_DIRECTORY+"/partial_vocabulary");
        FileUtils.deleteFolder(TEST_DIRECTORY);
    }

    @Test
    public void mergeIndexesTest() {

        Config config = ConfigUtils.getConfig();
        setPaths();
        setUp();
        Merger2 merger = Merger2.with(config, 1);


        try {
            merger.mergeIndexes(1, config.isCompressionEnabled(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tearDown();
    }

    @Test
    public void singleIndexMergeWithoutCompression() {

        Config config = ConfigUtils.getConfig();
        setPaths();
        setUp();

        MergerWithouCompression.mergeSingleIndex(config, false);
        tearDown();
    }
}

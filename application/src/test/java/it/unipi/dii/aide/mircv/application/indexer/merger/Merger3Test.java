package it.unipi.dii.aide.mircv.application.indexer.merger;

import it.unipi.dii.aide.mircv.application.config.*;
import it.unipi.dii.aide.mircv.application.data.VocabularyEntry;
import it.unipi.dii.aide.mircv.application.data.PostingList;
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
import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.BlockDescriptor;
import it.unipi.dii.aide.mircv.application.data.Posting;
import it.unipi.dii.aide.mircv.application.data.PostingList;
import it.unipi.dii.aide.mircv.application.data.VocabularyEntry;

import static org.junit.jupiter.api.Assertions.*;


import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import it.unipi.dii.aide.mircv.application.compression.UnaryCompressor;
import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.*;
import it.unipi.dii.aide.mircv.application.compression.VariableByteCompressor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.nio.MappedByteBuffer;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class Merger3Test {

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

    @BeforeAll
    static void setPaths(){
        //FileUtils.deleteFolder(TEST_DIRECTORY);
        FileUtils.createFolder(TEST_DIRECTORY);
        Merger3.setPathToVocabulary(VOCABULARY_PATH);
        Merger3.setPathToInvertedIndexDocs(INVERTED_INDEX_DOCIDS);
        Merger3.setPathToInvertedIndexFreqs(INVERTED_INDEX_FREQS);
        Merger3.setPathToBlockDescriptors(BLOCK_DESCRIPTOR_PATH);
        Merger3.setPathToPartialIndexesDocs(PATH_TO_PARTIAL_INDEXES_DOCS);
        Merger3.setPathToPartialIndexesFreqs(PATH_TO_PARTIAL_FREQUENCIES);
        Merger3.setPathToPartialVocabularies(PATH_TO_PARTIAL_VOCABULARY);
        VocabularyEntry.setBlockDescriptorsPath(BLOCK_DESCRIPTOR_PATH);
        BlockDescriptor.setInvertedIndexDocs(INVERTED_INDEX_DOCIDS);
        BlockDescriptor.setInvertedIndexFreqs(INVERTED_INDEX_FREQS);
        DocumentCollectionSize.setCollectionStatisticsPath(COLLECTION_STATISTICS_PATH);
        Vocabulary.setVocabularyPath(VOCABULARY_PATH);
        //if(Flags.isStemStopRemovalEnabled())
        //Preprocesser.readStopwords();
    }


    @BeforeEach
    void setUp() {
        //create directories to store partial frequencies, docids and vocabularies
        //FileUtils.createFolder(TEST_DIRECTORY);
        FileUtils.createFolder(TEST_DIRECTORY+"/partial_freqs");
        FileUtils.createFolder(TEST_DIRECTORY+"/partial_docids");
        FileUtils.createFolder(TEST_DIRECTORY+"/partial_vocabulary");
        BlockDescriptor.setMemoryOffset(0);
        Vocabulary.unsetInstance();
    }

    @AfterEach
    void tearDown() {
        //delete directories to store partial frequencies, docids and vocabularies
        FileUtils.deleteFolder(TEST_DIRECTORY+"/partial_freqs");
        FileUtils.deleteFolder(TEST_DIRECTORY+"/partial_docids");
        FileUtils.deleteFolder(TEST_DIRECTORY+"/partial_vocabulary");
        FileUtils.deleteFolder(TEST_DIRECTORY);
    }

    private boolean writeIntermediateIndexesToDisk(ArrayList<ArrayList<PostingList>> intermediateIndexes) {
        setPaths();
        setUp();

        for (ArrayList<PostingList> intermediateIndex : intermediateIndexes) {

            int i = intermediateIndexes.indexOf(intermediateIndex);

            try (
                    FileChannel docsFchan = (FileChannel) Files.newByteChannel(Paths.get(PATH_TO_PARTIAL_INDEXES_DOCS + "_"+i),
                            StandardOpenOption.WRITE,
                            StandardOpenOption.READ,
                            StandardOpenOption.CREATE
                    );
                    FileChannel freqsFchan = (FileChannel) Files.newByteChannel(Paths.get(PATH_TO_PARTIAL_FREQUENCIES +"_"+ i),
                            StandardOpenOption.WRITE,
                            StandardOpenOption.READ,
                            StandardOpenOption.CREATE);
                    FileChannel vocabularyFchan = (FileChannel) Files.newByteChannel(Paths.get(PATH_TO_PARTIAL_VOCABULARY +"_"+ i),
                            StandardOpenOption.WRITE,
                            StandardOpenOption.READ,
                            StandardOpenOption.CREATE)
            ) {
                long vocOffset = 0;
                long docidOffset = 0;
                long freqOffset = 0;
                for (PostingList postingList : intermediateIndex) {

                    int numPostings = intermediateIndex.size();
                    // instantiation of MappedByteBuffer for integer list of docids and for integer list of freqs
                    MappedByteBuffer docsBuffer = docsFchan.map(FileChannel.MapMode.READ_WRITE, docidOffset, numPostings * 4L);
                    MappedByteBuffer freqsBuffer = freqsFchan.map(FileChannel.MapMode.READ_WRITE, freqOffset, numPostings * 4L);

                    // check if MappedByteBuffers are correctly instantiated
                    if (docsBuffer != null && freqsBuffer != null) {
                        //create vocabulary entry
                        VocabularyEntry vocEntry = new VocabularyEntry(postingList.getTerm());
                        vocEntry.setDocIdOffset(docsBuffer.position());
                        vocEntry.setFrequencyOffset(docsBuffer.position());

                        // write postings to file
                        for (Posting posting : postingList.getPostings()) {
                            // encode docid and freq
                            docsBuffer.putInt(posting.getDocId());
                            freqsBuffer.putInt(posting.getFrequency());

                        }
                        vocEntry.updateStatistics(postingList);
                        vocEntry.setBM25Dl(postingList.getBM25Dl());
                        vocEntry.setBM25Tf(postingList.getBM25Tf());
                        vocEntry.setDocIdSize(numPostings*4);
                        vocEntry.setFrequencySize(numPostings*4);

                        vocEntry.setDocIdOffset(docidOffset);
                        vocEntry.setFrequencyOffset(freqOffset);

                        vocOffset = vocEntry.writeEntry(vocOffset, vocabularyFchan);

                        docidOffset += numPostings * 4L;
                        freqOffset += numPostings * 4L;

                    } else {
                        //tearDown();
                        return false;
                    }
                }
            } catch (Exception e) {
                //tearDown();
                return false;
            }
        }
        //tearDown();
        return true;
    }

    private ArrayList<ArrayList<Posting>> retrieveIndexFromDisk(Config config){
        setPaths();
        setUp();
        // get vocabulary from disk
        Vocabulary v = Vocabulary.with(config);

        v.readFromDisk();

        ArrayList<ArrayList<Posting>> mergedLists = new ArrayList<>(v.size());

        ArrayList<VocabularyEntry> vocEntries = new ArrayList<>();
        vocEntries.addAll(v.values());

        for(VocabularyEntry vocabularyEntry: vocEntries){
            PostingList p = new PostingList(config);
            p.setTerm(vocabularyEntry.getTerm());
            p.openList();
            ArrayList<Posting> postings = new ArrayList<>();

            while(p.next(config)!=null){
                postings.add(p.getCurrentPosting());
            }

            p.closeList();

            mergedLists.add(postings);
        }
        //tearDown();
        return mergedLists;
    }



    private LinkedHashMap<Integer, DocumentIndexEntry> buildDocIndex(Config config, ArrayList<ArrayList<PostingList>> indexes){
        setPaths();
        setUp();
        LinkedHashMap<Integer, DocumentIndexEntry> docIndex = new LinkedHashMap<>();
        int docCounter = 0;

        for(ArrayList<PostingList> index: indexes){
            for(PostingList postingList: index){
                for(Posting posting: postingList.getPostings()){
                    DocumentIndexEntry docEntry = docIndex.get(posting.getDocId());
                    if(docEntry!=null){
                        docEntry.setDocumentLength(docEntry.getDocumentLenght()+posting.getFrequency());
                    } else {
                        docEntry = new DocumentIndexEntry(config, Integer.toString(posting.getDocId()), docCounter, posting.getFrequency());
                        docIndex.put(posting.getDocId(), docEntry);
                        docCounter++;
                    }
                }
            }

        }
        //tearDown();
        return docIndex;
    }

    public boolean writeDocumentIndexToDisk(LinkedHashMap<Integer, DocumentIndexEntry> docIndex) {
        setPaths();
        setUp();

        // try to open a file channel to the file of the inverted index
        try (FileChannel fChan = (FileChannel) Files.newByteChannel(
                Paths.get(DOCINDEX_PATH),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE))
        {
            int memOffset = 0;
            for(DocumentIndexEntry documentIndexEntry: docIndex.values()){
                // instantiation of MappedByteBuffer for the entry
                MappedByteBuffer buffer = fChan.map(FileChannel.MapMode.READ_WRITE, memOffset, DocumentIndexEntry.ENTRY_SIZE);

                // Buffer not created
                if(buffer == null)
                    return false;

                // Create the CharBuffer with size = PID_SIZE
                CharBuffer charBuffer = CharBuffer.allocate(DocumentIndexEntry.DOC_ID_SIZE);
                for(int i = 0; i < documentIndexEntry.getPId().length(); i++)
                    charBuffer.put(i, documentIndexEntry.getPId().charAt(i));

                // Write the PID into file
                buffer.put(StandardCharsets.UTF_8.encode(charBuffer));

                // Write the docid into file
                buffer.putInt(documentIndexEntry.getDocumentId());
                // Write the doclen into file
                buffer.putInt(documentIndexEntry.getDocumentLenght());

                // update memory offset
                memOffset += DocumentIndexEntry.ENTRY_SIZE;
            }

        } catch(Exception e){
            e.printStackTrace();
            return false;
        }

        DocumentCollectionSize.updateCollectionSize(docIndex.size());
        DocumentCollectionSize.updateDocumentsLenght(22);
        //tearDown();
        return true;
    }


    public void mergeSingleIndex(Config config, Boolean compressionMode){
        setPaths();
        setUp();

        // building partial index 1
        ArrayList<PostingList> index1 = new ArrayList<>();

        index1.add(new PostingList(config, "alberobello\t1:3 2:3: 4:7"));
        index1.add(new PostingList(config, "newyork\t1:5 3:2: 4:6"));
        index1.add(new PostingList(config, "pisa\t1:1 5:3"));

        // insert partial index to array of partial indexes
        ArrayList<ArrayList<PostingList>> intermediateIndexes = new ArrayList<>();
        intermediateIndexes.add(index1);

        // build document index for intermediate indexes
        LinkedHashMap<Integer, DocumentIndexEntry> docIndex = buildDocIndex(config, intermediateIndexes);

        // write document index to disk
        assertTrue(writeDocumentIndexToDisk(docIndex), "Error while writing document index to disk");

        // write intermediate indexes to disk so that SPIMI can be executed
        assertTrue(writeIntermediateIndexesToDisk(intermediateIndexes), "Error while writing intermediate indexes to disk");

        // merging intermediate indexes
        assertTrue(Merger3.mergeIndexes(intermediateIndexes.size(), compressionMode, false), "Error: merging failed");

        ArrayList<ArrayList<Posting>> mergedLists = retrieveIndexFromDisk(config);

        assertNotNull(mergedLists, "Error, merged index is empty");

        // build expected results
        ArrayList<ArrayList<Posting>> expectedResults = new ArrayList<>(3);

        ArrayList<Posting> postings = new ArrayList<>();
        postings.addAll(List.of(new Posting[]{
                new Posting(1, 3),
                new Posting(2, 3),
                new Posting(4,7)
        }));
        expectedResults.add(postings);
        postings = new ArrayList<>();
        postings.addAll(List.of(new Posting[]{
                new Posting(1,5),
                new Posting(3,2),
                new Posting(4,6)
        }));
        expectedResults.add(postings);

        postings = new ArrayList<>();
        postings.addAll(List.of(new Posting[]{
                new Posting(1,1),
                new Posting(5,3)
        }));
        expectedResults.add(postings);

        assertEquals(expectedResults.toString(), mergedLists.toString(), "Error, expected results are different from actual results.");
        //tearDown();
    }

    private void mergeTwoIndexes(Config config, boolean compressionMode, boolean vocabularyTest) {
        setPaths();
        setUp();
        // building partial index 1
        ArrayList<PostingList> postings = new ArrayList<>();

        PostingList pl = new PostingList(config, "amburgo\t1:3 2:2: 3:5");
        pl.updateBM25Parameters(1,3);
        postings.add(pl);
        pl = new PostingList(config, "pisa\t2:1 3:2");
        pl.updateBM25Parameters(4,1);
        postings.add(pl);
        pl = new PostingList(config, "zurigo\t2:1 3:2");
        pl.updateBM25Parameters(4,1);
        postings.add(pl);

        // building partial index 2
        ArrayList<PostingList> index2 = new ArrayList<>();
        pl = new PostingList(config, "alberobello\t4:3 5:1");
        pl.updateBM25Parameters(1,3);
        index2.add(pl);
        pl = new PostingList(config, "pisa\t5:2");
        pl.updateBM25Parameters(3, 2);
        index2.add(pl);

        // insert partial index to array of partial indexes
        ArrayList<ArrayList<PostingList>> intermediateIndexes = new ArrayList<>();
        intermediateIndexes.add(postings);
        intermediateIndexes.add(index2);

        // build document index for intermediate indexes
        LinkedHashMap<Integer, DocumentIndexEntry> docIndex = buildDocIndex(config, intermediateIndexes);

        // write document index to disk
        assertTrue(writeDocumentIndexToDisk(docIndex), "Error while writing document index to disk");

        // write intermediate indexes to disk
        assertTrue(writeIntermediateIndexesToDisk(intermediateIndexes), "Error while writing intermediate indexes to disk");

        // merging intermediate indexes
        assertTrue(Merger3.mergeIndexes(intermediateIndexes.size(), compressionMode, false), "Error: merging failed");

        if(vocabularyTest){
            ArrayList<VocabularyEntry> expectedVocabulary = new ArrayList<>();
            VocabularyEntry vocEntry = new VocabularyEntry("alberobello");
            if(compressionMode){
                vocEntry.setDocIdSize(0);
                vocEntry.setFrequencySize(0);
                vocEntry.setDocIdOffset(0);
                vocEntry.setFrequencyOffset(0);
            } else {
                vocEntry.setDocIdSize(0);
                vocEntry.setFrequencySize(0);
                vocEntry.setDocIdOffset(0);
                vocEntry.setFrequencyOffset(0);
            }
            vocEntry.setNumBlocks(1);
            vocEntry.setBlockOffset(0);
            vocEntry.setDocumentFrequency(2);
            vocEntry.setInverseDocumentFrequency(0.3979400086720376);
            vocEntry.setMaxTermFrequency(3);
            vocEntry.setMaxTfIdf(0.5878056449127935);
            vocEntry.setBM25Tf(3);
            vocEntry.setBM25Dl(1);
            vocEntry.setMaxBM25Tf(0.3288142794660968);

            expectedVocabulary.add(vocEntry);


            vocEntry = new VocabularyEntry("amburgo");
            if(compressionMode){
                vocEntry.setDocIdSize(0);
                vocEntry.setFrequencySize(0);
                vocEntry.setDocIdOffset(2);
                vocEntry.setFrequencyOffset(1);
            } else {
                vocEntry.setDocIdSize(0);
                vocEntry.setFrequencySize(0);
                vocEntry.setDocIdOffset(8);
                vocEntry.setFrequencyOffset(8);
            }
            vocEntry.setNumBlocks(1);
            vocEntry.setBlockOffset(BlockDescriptor.BLOCK_DESCRIPTOR_ENTRY_BYTES);
            vocEntry.setDocumentFrequency(3);
            vocEntry.setInverseDocumentFrequency(0.22184874961635637);
            vocEntry.setMaxTermFrequency(5);
            vocEntry.setMaxTfIdf(0.3769143710976413);
            vocEntry.setBM25Tf(3);
            vocEntry.setBM25Dl(1);
            vocEntry.setMaxBM25Tf(0.18331164287548693);

            expectedVocabulary.add(vocEntry);


            vocEntry = new VocabularyEntry("pisa");
            if(compressionMode){
                vocEntry.setDocIdSize(0);
                vocEntry.setFrequencySize(0);
                vocEntry.setDocIdOffset(5);
                vocEntry.setFrequencyOffset(3);
            } else {
                vocEntry.setDocIdSize(0);
                vocEntry.setFrequencySize(0);
                vocEntry.setDocIdOffset(20);
                vocEntry.setFrequencyOffset(20);
            }
            vocEntry.setNumBlocks(1);
            vocEntry.setBlockOffset(BlockDescriptor.BLOCK_DESCRIPTOR_ENTRY_BYTES*2);
            vocEntry.setDocumentFrequency(3);
            vocEntry.setInverseDocumentFrequency(0.22184874961635637);
            vocEntry.setMaxTermFrequency(2);
            vocEntry.setMaxTfIdf(0.2886318777514278);
            vocEntry.setBM25Tf(2);
            vocEntry.setBM25Dl(3);
            vocEntry.setMaxBM25Tf(0.1412129473145704);

            expectedVocabulary.add(vocEntry);


            vocEntry = new VocabularyEntry("zurigo");
            if(compressionMode){
                vocEntry.setDocIdSize(0);
                vocEntry.setFrequencySize(0);
                vocEntry.setDocIdOffset(8);
                vocEntry.setFrequencyOffset(4);
            } else {
                vocEntry.setDocIdSize(0);
                vocEntry.setFrequencySize(0);
                vocEntry.setDocIdOffset(32);
                vocEntry.setFrequencyOffset(32);
            }

            vocEntry.setNumBlocks(1);
            vocEntry.setBlockOffset(BlockDescriptor.BLOCK_DESCRIPTOR_ENTRY_BYTES*3);
            vocEntry.setDocumentFrequency(2);
            vocEntry.setInverseDocumentFrequency(0.3979400086720376);
            vocEntry.setMaxTermFrequency(2);
            vocEntry.setMaxTfIdf(0.5177318877571058);
            vocEntry.setBM25Tf(1);
            vocEntry.setBM25Dl(4);
            vocEntry.setMaxBM25Tf(0.16596550124710574);

            expectedVocabulary.add(vocEntry);


            // get vocabulary from disk
            Vocabulary v = Vocabulary.with(config);
            v.readFromDisk();

            ArrayList<VocabularyEntry> retrievedVocabulary = new ArrayList<>();
            retrievedVocabulary.addAll(v.values());

            assertArrayEquals(expectedVocabulary.toArray(), retrievedVocabulary.toArray(), "Vocabulary after merging is different from the expected vocabulary.");

        } else {

            ArrayList<ArrayList<Posting>> mergedLists = retrieveIndexFromDisk(config);

            // build expected results
            ArrayList<ArrayList<Posting>> expectedResults = new ArrayList<>(4);

            ArrayList<Posting> expectedPostings = new ArrayList<>();
            expectedPostings.addAll(List.of(new Posting[]{
                    new Posting(4,3),
                    new Posting(5,1),
            }));
            expectedResults.add(expectedPostings);
            expectedPostings = new ArrayList<>();

            expectedPostings.addAll(List.of(new Posting[]{
                    new Posting(1, 3),
                    new Posting(2, 2),
                    new Posting(3,5)
            }));
            expectedResults.add(expectedPostings);

            expectedPostings = new ArrayList<>();
            expectedPostings.addAll(List.of(new Posting[]{
                    new Posting(2,1),
                    new Posting(3,2),
                    new Posting(5,2)
            }));
            expectedResults.add(expectedPostings);
            expectedPostings = new ArrayList<>();
            expectedPostings.addAll(List.of(new Posting[]{
                    new Posting(2,1),
                    new Posting(3,2),
            }));
            expectedResults.add(expectedPostings);

            assertEquals(expectedResults.toString(), mergedLists.toString(), "Error, expected results are different from actual results.");
        }
        //tearDown();



    }


    /* test merging of a single index without compression
     *      index tested:
     *          - "alberobello" = {(1,3), (2,3), (4,7)}
     *          - "newyork" = {(1,5), (3,2), (4,6)}
     *          - "pisa" = {(1,1), (5,3)}
     */
    @Test
    public void singleIndexMergeWithoutCompression(){
        Config config = ConfigUtils.getConfig();
        setPaths();
        setUp();
        config.getBlockDescriptorConfig().setCompressionEnabled(false);
        mergeSingleIndex(config, false);
        //tearDown();
    }

    /* test merging of a single index with compression
     *      index tested:
     *          - "alberobello" = {(1,3), (2,3), (4,7)}
     *          - "newyork" = {(1,5), (3,2), (4,6)}
     *          - "pisa" = {(1,1), (5,3)}
     */
    @Test
    public void singleIndexMergeWithCompression() {
        Config config = ConfigUtils.getConfig();
        setPaths();
        setUp();
        config.getBlockDescriptorConfig().setCompressionEnabled(true);
        mergeSingleIndex(config, true);
        //tearDown();
    }


    /* test merging of two indexes without compression
     *      index 1:
     *          - "amburgo" = {(1,3), (2,2), (3,5)}
     *          - "pisa" = {(2,1), (3,2)}
     *          - "zurigo" = {(4,1)}
     *      index 2:
     *          - "alberobello" = {(4,3), (5,1)}
     *          - "pisa" = {(5,2)}
     */
    @Test
    public void twoIndexesMergeWithoutCompression() {
        Config config = ConfigUtils.getConfig();
        setPaths();
        setUp();
        config.getBlockDescriptorConfig().setCompressionEnabled(false);
        mergeTwoIndexes(config, false, false);
        //tearDown();

    }

    /* test merging of two indexes with compression
     *      index 1:
     *          - "amburgo" = {(1,3), (2,2), (3,5)}
     *          - "pisa" = {(2,1), (3,2)}
     *          - "zurigo" = {(4,1)}
     *      index 2:
     *          - "alberobello" = {(4,3), (5,1)}
     *          - "pisa" = {(5,2)}
     */
    @Test
    public void twoIndexesMergeWithCompression() {
        Config config = ConfigUtils.getConfig();
        setPaths();
        setUp();
        config.getBlockDescriptorConfig().setCompressionEnabled(true);
        mergeTwoIndexes(config, true, true);
        //tearDown();

    }

    /* test vocabulary after merging of two indexes without compression
     *      index 1:
     *          - "amburgo" = {(1,3), (2,2), (3,5)}
     *          - "pisa" = {(2,1), (3,2)}
     *          - "zurigo" = {(4,1)}
     *      index 2:
     *          - "alberobello" = {(4,3), (5,1)}
     *          - "pisa" = {(5,2)}
     */
    @Test
    public void vocabularyTest(){
        Config config = ConfigUtils.getConfig();
        setPaths();
        setUp();
        config.getBlockDescriptorConfig().setCompressionEnabled(false);
        mergeTwoIndexes(config, false, true);
        //tearDown();

    }

    /* test vocabulary after merging of two indexes without compression
     *      index 1:
     *          - "amburgo" = {(1,3), (2,2), (3,5)}
     *          - "pisa" = {(2,1), (3,2)}
     *          - "zurigo" = {(4,1)}
     *      index 2:
     *          - "alberobello" = {(4,3), (5,1)}
     *          - "pisa" = {(5,2)}
     */
    @Test
    public void vocabularyTest2() {
        Config config = ConfigUtils.getConfig();
        setPaths();
        setUp();
        config.getBlockDescriptorConfig().setCompressionEnabled(true);
        mergeTwoIndexes(config, true, true);
        //tearDown();
    }




}
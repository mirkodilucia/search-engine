package it.unipi.dii.aide.mircv.application.indexer.merger;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.*;
import it.unipi.dii.aide.mircv.application.indexer.Merger2;
import it.unipi.dii.aide.mircv.application.utils.FileUtils;

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

import static org.junit.jupiter.api.Assertions.*;

public class MergerWithouCompression {


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
        //FileUtils.deleteFolder(TEST_DIRECTORY+"/partial_freqs");
        //FileUtils.deleteFolder(TEST_DIRECTORY+"/partial_docids");
        //FileUtils.deleteFolder(TEST_DIRECTORY+"/partial_vocabulary");
        //FileUtils.deleteFolder(TEST_DIRECTORY);
    }



    private static LinkedHashMap<Integer, DocumentIndexEntry> buildDocumentIndex(Config config, ArrayList<ArrayList<PostingList>> indexes){
        setPaths();
        setUp();


        LinkedHashMap<Integer, DocumentIndexEntry> docIndex = new LinkedHashMap<>();
        int docCounter = 0;

        for(ArrayList<PostingList> index: indexes){
            for(PostingList postingList: index){
                for(Posting posting: postingList.getPostings()){
                    DocumentIndexEntry docEntry = docIndex.get(posting.getDocId());
                    if(docEntry != null){
                        docEntry.setDocumentLength(docEntry.getDocumentLenght()+posting.getFrequency());
                    } else {
                        docEntry = new DocumentIndexEntry(config, Integer.toString(posting.getDocId()), docCounter, posting.getFrequency());
                        docIndex.put(posting.getDocId(), docEntry);
                        docCounter++;
                    }
                }
            }
        }
        tearDown();
        return docIndex;
    }

    private static ArrayList<ArrayList<Posting>> retrieveIndexFromDisk(Config config){
        setPaths();
        setUp();
        // get vocabulary from disk
        Vocabulary v = Vocabulary.with(config);

        v.readFromDisk();
        System.out.println(v.getPath());

        ArrayList<ArrayList<Posting>> mergedLists = new ArrayList<>(v.size());
        ArrayList<VocabularyEntry> vocEntries = new ArrayList<>(v.values());

        for(VocabularyEntry vocabularyEntry: vocEntries){
            PostingList p = new PostingList(config);

            System.out.println(v.getPath());


            p.setTerm(vocabularyEntry.getTerm());
            p.openList();
            ArrayList<Posting> postings = new ArrayList<>();

            while(p.next(config) != null){
                postings.add(p.getCurrentPosting());
            }

            p.closeList();

            mergedLists.add(postings);
        }
        tearDown();
        return mergedLists;
    }

    public static boolean writeDocumentIndexToDisk(Config config, LinkedHashMap<Integer, DocumentIndexEntry> docIndex) {
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
            tearDown();
            return false;
        }

        DocumentCollectionSize.setCollectionSize(docIndex.size());
        DocumentCollectionSize.setTotalDocumentLen(22);
        tearDown();
        return true;
    }

    private static boolean writeIntermediateIndexesToDisk(Config config, ArrayList<ArrayList<PostingList>> intermediateIndexes) {
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
                        tearDown();
                        return false;
                    }
                }
            } catch (Exception e) {
                tearDown();
                return false;
            }
        }
        tearDown();
        return true;
    }

    public static void mergeSingleIndex(Config config, Boolean compressionMode){
        setPaths();
        setUp();
        // building partial index 1
        ArrayList<PostingList> index1 = new ArrayList<>();

        index1.add(new PostingList(config,"alberobello\t1:3 2:3: 4:7"));
        index1.add(new PostingList(config, "newyork\t1:5 3:2: 4:6"));
        index1.add(new PostingList(config, "pisa\t1:1 5:3"));

        // insert partial index to array of partial indexes
        ArrayList<ArrayList<PostingList>> intermediateIndexes = new ArrayList<>();
        intermediateIndexes.add(index1);

        // build document index for intermediate indexes
        LinkedHashMap<Integer, DocumentIndexEntry> docIndex = buildDocumentIndex(config, intermediateIndexes);

        // write document index to disk
        assertTrue(writeDocumentIndexToDisk(config, docIndex), "Error while writing document index to disk");

        // write intermediate indexes to disk so that SPIMI can be executed
        assertTrue(writeIntermediateIndexesToDisk(config, intermediateIndexes), "Error while writing intermediate indexes to disk");

        // merging intermediate indexes
        Merger2 merger = Merger2.with(config, intermediateIndexes.size());
        assertTrue(merger.mergeIndexes(intermediateIndexes.size(), compressionMode, false), "Error: merging failed");

        ArrayList<ArrayList<Posting>> mergedLists = retrieveIndexFromDisk(config);

        assertNotNull(mergedLists, "Error, merged index is empty");

        // build expected results
        ArrayList<ArrayList<Posting>> expectedResults = new ArrayList<>(3);

        ArrayList<Posting> postings = new ArrayList<>(List.of(new Posting[]{
                new Posting(1, 3),
                new Posting(2, 3),
                new Posting(4, 7)
        }));
        expectedResults.add(postings);
        postings = new ArrayList<>(List.of(new Posting[]{
                new Posting(1, 5),
                new Posting(3, 2),
                new Posting(4, 6)
        }));
        expectedResults.add(postings);

        postings = new ArrayList<>(List.of(new Posting[]{
                new Posting(1, 1),
                new Posting(5, 3)
        }));
        expectedResults.add(postings);

        assertEquals(expectedResults.toString(), mergedLists.toString(), "Error, expected results are different from actual results.");
        tearDown();
    }

}

package indexer.merger;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.document.table.DocumentIndexEntry;
import it.unipi.dii.aide.mircv.indexer.merger.Merger;
import it.unipi.dii.aide.mircv.indexer.model.Posting;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;

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

public class MergerWithoutCompression {

    protected static String PATH_TO_PARTIAL_VOCABULARIES = "data_test/mergerWorkerTest/vocabulary_0.dat";
    protected static String PATH_TO_PARTIAL_INDEXES_DOCS = "data_test/mergerWorkerTest/partial_index_docs_";
    protected static String PATH_TO_PARTIAL_INDEXES_FREQS = "data_test/mergerWorkerTest/partial_index_freqs_";

    private static void setupPath(Config config) {
        PATH_TO_PARTIAL_VOCABULARIES = config.getPartialVocabularyPath();
        PATH_TO_PARTIAL_INDEXES_DOCS = config.getPartialIndexesDocumentsPath();
        PATH_TO_PARTIAL_INDEXES_FREQS = config.getPartialIndexesFrequenciesPath();
    }

    private static LinkedHashMap<Integer, DocumentIndexEntry> buildDocumentIndex(Config config, ArrayList<ArrayList<PostingList>> indexes){
        LinkedHashMap<Integer, DocumentIndexEntry> docIndex = new LinkedHashMap<>();
        int docCounter = 0;

        for(ArrayList<PostingList> index: indexes){
            for(PostingList postingList: index){
                for(Posting posting: postingList.getPostings()){
                    DocumentIndexEntry docEntry = docIndex.get(posting.getDocumentId());
                    if(docEntry != null){
                        docEntry.setDocumentLength(docEntry.getDocumentLenght()+posting.getFrequency());
                    } else {
                        docEntry = new DocumentIndexEntry(config, Integer.toString(posting.getDocumentId()), docCounter, posting.getFrequency());
                        docIndex.put(posting.getDocumentId(), docEntry);
                        docCounter++;
                    }
                }
            }
        }
        return docIndex;
    }

    public static ArrayList<ArrayList<Posting>> retrieveIndexFromDisk(Config config) {
        // get vocabulary from disk
        Vocabulary v = Vocabulary.with(config);
        v.readFromDisk();

        ArrayList<ArrayList<Posting>> mergedLists = new ArrayList<>(v.size());
        ArrayList<VocabularyEntry> vocEntries = new ArrayList<>(v.values());

        for(VocabularyEntry vocabularyEntry: vocEntries) {
            PostingList p = new PostingList(config, vocabularyEntry.getTerm());

            p.openList();
            ArrayList<Posting> postings = new ArrayList<>();

            while(p.next() != null) {
                Posting currPosting = p.getCurrentPosting();
                postings.add(currPosting);
            }

            p.closeList();

            mergedLists.add(postings);
        }
        return mergedLists;
    }

    public static boolean writeDocumentIndexToDisk(Config config, LinkedHashMap<Integer, DocumentIndexEntry> docIndex) {

        // try to open a file channel to the file of the inverted index
        try (FileChannel fChan = (FileChannel) Files.newByteChannel(
                Paths.get(config.getInvertedIndexDocs()),
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

        DocumentIndexState.setCollectionSize(docIndex.size());
        DocumentIndexState.setTotalDocumentLen(22);
        return true;
    }

    public static boolean writeIntermediateIndexesToDisk(Config config, ArrayList<ArrayList<PostingList>> intermediateIndexes) {
        for (ArrayList<PostingList> intermediateIndex : intermediateIndexes) {

            int i = intermediateIndexes.indexOf(intermediateIndex);

            try (
                    FileChannel docsFchan = FileChannelHandler.open(PATH_TO_PARTIAL_INDEXES_DOCS + "_" + i + ".dat",
                            StandardOpenOption.WRITE,
                            StandardOpenOption.READ,
                            StandardOpenOption.CREATE
                    );
                    FileChannel freqsFchan = FileChannelHandler.open(PATH_TO_PARTIAL_INDEXES_FREQS + "_" + i + ".dat",
                            StandardOpenOption.WRITE,
                            StandardOpenOption.READ,
                            StandardOpenOption.CREATE);

                    FileChannel vocabularyFchan = FileChannelHandler.open(PATH_TO_PARTIAL_VOCABULARIES + "_" + i + ".dat",
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
                        vocEntry.setDocumentIdOffset(docsBuffer.position());
                        vocEntry.setFrequencyOffset(docsBuffer.position());

                        // write postings to file
                        for (Posting posting : postingList.getPostings()) {
                            // encode docid and freq
                            docsBuffer.putInt(posting.getDocumentId());
                            freqsBuffer.putInt(posting.getFrequency());

                        }
                        vocEntry.updateStatistics(postingList);
                        vocEntry.setBM25Dl(postingList.getBM25Dl());
                        vocEntry.setBM25Tf(postingList.getBM25Tf());
                        vocEntry.updateMemoryIdSize(numPostings);

                        vocEntry.setDocumentIdOffset((int) docidOffset);
                        vocEntry.setFrequencyOffset((int) freqOffset);

                        vocOffset = vocEntry.writeEntry(vocOffset, vocabularyFchan);

                        docidOffset += numPostings * 4L;
                        freqOffset += numPostings * 4L;

                    } else {
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static void mergeSingleIndex(Config config){
        setupPath(config);

        // building partial index 1
        ArrayList<PostingList> index1 = new ArrayList<>();

        index1.add(new PostingList(config,"alberobello\t1:3 2:3 4:7"));
        index1.add(new PostingList(config,"newyork\t1:5 3:2 4:6"));
        index1.add(new PostingList(config,"pisa\t1:1 5:3"));

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
        Merger merger = Merger.with(config);
        assertTrue(merger.mergeIndexes(intermediateIndexes.size()), "Error: merging failed");

        ArrayList<ArrayList<Posting>> mergedLists = retrieveIndexFromDisk(config);

        assertNotNull(mergedLists, "Error, merged index is empty");

        ArrayList<ArrayList<Posting>> expectedResults = getPostingsResultForSingleIndex();

        assertEquals(expectedResults.toString(), mergedLists.toString(), "Error, expected results are different from actual results.");
    }

    public static void mergeTwoIndexes(Config config) {
        setupPath(config);

        ArrayList<PostingList> index1 = new ArrayList<>();

        PostingList pl = new PostingList(config,"amburgo\t1:3 2:2: 3:5");
        pl.updateBM25Parameters(1,3);
        index1.add(pl);
        pl = new PostingList(config,"pisa\t2:1 3:2");
        pl.updateBM25Parameters(4,1);
        index1.add(pl);
        pl = new PostingList(config,"zurigo\t2:1 3:2");
        pl.updateBM25Parameters(4,1);
        index1.add(pl);

        // building partial index 2
        ArrayList<PostingList> index2 = new ArrayList<>();
        pl = new PostingList(config, "alberobello\t4:3 5:1");
        pl.updateBM25Parameters(1,3);
        index2.add(pl);
        pl = new PostingList(config,"pisa\t5:2");
        pl.updateBM25Parameters(3, 2);
        index2.add(pl);

        // insert partial index to array of partial indexes
        ArrayList<ArrayList<PostingList>> intermediateIndexes = new ArrayList<>();
        intermediateIndexes.add(index1);
        intermediateIndexes.add(index2);

        // build document index for intermediate indexes
        LinkedHashMap<Integer, DocumentIndexEntry> docIndex = MergerWithoutCompression.buildDocIndex(config, intermediateIndexes);

        // write document index to disk
        assertTrue(writeDocumentIndexToDisk(config, docIndex), "Error while writing document index to disk");

        // write intermediate indexes to disk
        assertTrue(writeIntermediateIndexesToDisk(config, intermediateIndexes), "Error while writing intermediate indexes to disk");

        // merging intermediate indexes
        Merger merger = Merger.with(config);
        assertTrue(merger.mergeIndexes(intermediateIndexes.size()), "Error: merging failed");
    }

    public static LinkedHashMap<Integer, DocumentIndexEntry> buildDocIndex(Config config, ArrayList<ArrayList<PostingList>> indexes){
        LinkedHashMap<Integer, DocumentIndexEntry> docIndex = new LinkedHashMap<>();
        int docCounter = 0;

        for(ArrayList<PostingList> index: indexes){
            for(PostingList postingList: index){
                for(Posting posting: postingList.getPostings()){
                    DocumentIndexEntry docEntry = docIndex.get(posting.getDocumentId());
                    if(docEntry!=null){
                        docEntry.setDocumentLength(docEntry.getDocumentLenght() + posting.getFrequency());
                    } else {
                        docEntry = new DocumentIndexEntry(config, Integer.toString(posting.getDocumentId()), docCounter, posting.getFrequency());
                        docIndex.put(posting.getDocumentId(), docEntry);
                        docCounter++;
                    }
                }
            }

        }
        return docIndex;
    }

    private static ArrayList<ArrayList<Posting>> getPostingsResultForSingleIndex() {
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
        return expectedResults;
    }

    public static ArrayList<ArrayList<Posting>> getPostingsResultForTwoIndex() {
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

        return expectedResults;
    }

}
package it.unipi.dii.aide.mircv.application.indexer.spimi;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.*;
import it.unipi.dii.aide.mircv.application.indexer.FileChannelUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import it.unipi.dii.aide.mircv.application.utils.FileUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.nio.file.StandardOpenOption;
import java.nio.file.InvalidPathException;
import java.io.IOException;
import java.util.stream.Collectors;
import java.io.BufferedReader;

import java.nio.MappedByteBuffer;
import java.util.*;


public class Spimi implements SpimiListener {

    private static long MEMORY_LIMIT = 0;

    //number of partial indexes created
    private int numIndexes = 0;
    //number of partial indexes to write
    private long numPostings = 0;

    //configuration
    private final Config config;

    boolean allDocumentsProcessed = false;
    int documentsLength = 0;
    int documentId = 1;

    private Spimi(Config config) {
        this.config = config;
        numIndexes = 0;
        DocumentIndexEntry.reset();

        MEMORY_LIMIT = Math.round(Runtime.getRuntime().totalMemory() * 0.2);
    }

    public int executeSpimi() {
        int numIndexes = 0;

        try (BufferedReader bufferReader = initBuffer(config.isCompressionEnabled())) {
            boolean writeResult = false;

            while (!allDocumentsProcessed) {
                HashMap<String, PostingList> index = spimiIteration(bufferReader, documentId);
                writeResult = saveIndexToDisk(index, config.isDebugEnabled());

                if (!writeResult) {
                    System.out.println("Couldn't write index to disk.");
                    cleanup();
                    return -1;
                }
            }

            if(!DocumentCollectionSize.updateStatistics(documentId - 1,
                    documentsLength,
                    config.getCollectionStatisticsPath())){
                System.out.println("Couldn't update collection statistics.");
                return 0;
            }

            return numIndexes;
        }catch (Exception e){
            e.printStackTrace();
        }

        return numIndexes;
    }

    private HashMap<String, PostingList> spimiIteration(BufferedReader bufferedReader,
                                                        int documentId) throws IOException {

        HashMap<String, PostingList> index = new HashMap<>();
        while (Runtime.getRuntime().freeMemory() > MEMORY_LIMIT) {
            String line;

            if ((line = bufferedReader.readLine()) == null) {
                onSpimiFinished();
                break;
            }

            if (line.isBlank()) {
                continue;
            }

            String[] fields = line.split("\t");

            InitialDocument intialDocument = new InitialDocument(config, fields[0], fields[1].replaceAll("[^\\x00-\\x7F]", ""));
            FinalDocument finalDocument = intialDocument.processDocument();
            if (finalDocument.getTokens().isEmpty())
                continue;

            int documentsLength = finalDocument.getTokens().size();
            DocumentIndexEntry documentIndexEntry = new DocumentIndexEntry(
                    finalDocument.getPid(),
                    documentId,
                    documentsLength
            );

            // Send the document to the indexer
            this.updateDocumentLength(documentIndexEntry.getDocumentLenght());
            documentIndexEntry.writeFile();
            // TODO: if (config.isDebugEnabled()) documentIndexEntry.debugWriteToDisk("docIndex.txt");

            for (String term: finalDocument.getTokens()) {
                if (term.isBlank() || term.isEmpty()) {
                    continue;
                }

                PostingList postingList;
                if (!index.containsKey(term)) {
                    postingList = new PostingList(config, term);
                    index.put(term, postingList);
                    continue;
                }

                postingList = index.get(term);
                updateOrAddPosting(documentId, postingList);
                postingList.updateBM25Parameters(documentsLength, postingList.getPostings().size());
            }
        }
        return index;
    }


    /**
     * @param compressed  flag for compressed reading
     * @return buffer reader
     * initializes the buffer from which the entries are read, collecting from the compressed collection
     * */
    private BufferedReader initBuffer(boolean compressed) throws IOException {
        if(compressed) { //read from compressed collection
            TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(config.getPathToCompressedCollection())));
            tarInput.getNextTarEntry();
            return new BufferedReader(new InputStreamReader(tarInput, StandardCharsets.UTF_8));
        }

        return Files.newBufferedReader(Paths.get(config.getRawCollectionPath()), StandardCharsets.UTF_8);
    }


    /**
     * writes the partial index on file
     *
     * @param index: partial index that must be saved onto file
     */
    private boolean saveIndexToDisk(HashMap<String, PostingList> index, boolean debugMode) {
        System.out.println("saving index: "+numIndexes+" of size: "+index.size());

        if (index.isEmpty()){
            //if the index is empty there is nothing to write on disk
            System.out.println("empty index");
            return true;
        }


        //sort index in lexicographic order
        index = index.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        // try to open a file channel to the file of the inverted index
        try (
                FileChannel docsFchan = FileChannelUtils.openFileChannel(config.getPartialIndexDocsPath(numIndexes),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE
                );
                FileChannel freqsFchan = FileChannelUtils.openFileChannel(config.getPartialIndexFreqsPath(numIndexes),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                FileChannel vocabularyFchan = FileChannelUtils.openFileChannel(config.getPartialVocabularyPath(numIndexes),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE)
        ) {
            // instantiation of MappedByteBuffer for integer list of docids
            MappedByteBuffer docsBuffer = docsFchan.map(FileChannel.MapMode.READ_WRITE, 0, numPostings * 4L);

            // instantiation of MappedByteBuffer for integer list of freqs
            MappedByteBuffer freqsBuffer = freqsFchan.map(FileChannel.MapMode.READ_WRITE, 0, numPostings * 4L);

            long vocOffset = 0;
            // check if MappedByteBuffers are correctly instantiated
            if (docsBuffer != null && freqsBuffer != null) {
                for (PostingList list : index.values()) {
                    //create vocabulary entry
                    VocabularyEntry vocEntry = new VocabularyEntry(list.getTerm());
                    vocEntry.setDocidOffset(docsBuffer.position());
                    vocEntry.setFrequencyOffset(docsBuffer.position());

                    // write postings to file
                    for (Posting posting : list.getPostings()) {
                        // encode docid
                        docsBuffer.putInt(posting.getDocId());
                        // encode freq
                        freqsBuffer.putInt(posting.getFrequency());
                    }
                    vocEntry.updateStatistics(list);
                    vocEntry.setBM25Dl(list.getBM25Dl());
                    vocEntry.setBM25Tf(list.getBM25Tf());
                    vocEntry.setDocIdSize((int) (numPostings*4));
                    vocEntry.setFrequencySize((int) (numPostings*4));

                    vocOffset = vocEntry.writeEntry(vocOffset, vocabularyFchan);
                    if(debugMode){
                        list.debugSaveToDisk("partialDOCIDS_"+numIndexes+".txt", "partialFREQS_"+numIndexes+".txt", (int) numPostings);
                        vocEntry.debugSaveToDisk("partialVOC_"+numIndexes+".txt");
                    }
                }
            }

            //update number of partial inverted indexes and vocabularies
            numIndexes++;
            numPostings = 0;
            return true;

        } catch (InvalidPathException e) {
            System.out.println("Path Error " + e);
            return false;
        } catch (IOException e) {
            System.out.println("I/O Error " + e);
            return false;
        }
    }

    /**
     * Function that searched for a given docid in a posting list.
     * If the document is already present it updates the term frequency for that
     * specific document, if that's not the case creates a new pair (docid,freq)
     * in which frequency is set to 1 and adds this pair to the posting list
     *
     * @param docId:       docid of a certain document
     * @param postingList: posting list of a given term
     **/
    protected void updateOrAddPosting(int docId, PostingList postingList) {
        if (!postingList.getPostings().isEmpty()) {
            // last document inserted:
            Posting posting = postingList.getPostings().get(postingList.getPostings().size() - 1);
            //If the docId is the same I update the posting
            if (docId == posting.getDocId()) {
                posting.setFrequency(posting.getFrequency() + 1);
                return;
            }
        }

        // the document has not been processed (docIds are incremental):
        // create new pair and add it to the posting list
        postingList.getPostings().add(new Posting(docId, 1));

        //increment the number of postings
        numPostings++;
    }

    /**
     * cleaning directories containing partial data structures and document Index file
     */
    private void cleanup(){
        FileUtils.deleteFolder(config.getDocumentIndexPath());
        FileUtils.deleteFolder(config.getFrequencyFolder());
        FileUtils.deleteFolder(config.getPartialVocabularyFolder());
        FileUtils.removeFile(config.getDocumentIndexPath());
    }

    //obtain spimi instance with configuration
    public static Spimi with(Config config) {
        return new Spimi(config);
    }

    @Override
    public void updateDocumentId(String pid) {
        documentId++;

        if((documentId % 1000000) == 0){
            System.out.println("at docid: "+ documentId);
        }
    }

    @Override
    public void updateDocumentLength(int length) {
        documentsLength += length;
    }

    @Override
    public void onSpimiFinished() {
        this.allDocumentsProcessed = true;
    }
}

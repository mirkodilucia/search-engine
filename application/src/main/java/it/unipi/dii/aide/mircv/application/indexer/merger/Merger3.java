package it.unipi.dii.aide.mircv.application.indexer.merger;

import it.unipi.dii.aide.mircv.application.compression.UnaryCompressor;
import it.unipi.dii.aide.mircv.application.compression.VariableByteCompressor;
import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.*;
import it.unipi.dii.aide.mircv.application.indexer.FileChannelUtils;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

public class Merger3 {

    private static long freqsMemoryOffset;
    private static long docsMemoryOffset;
    private static int numIndexes;

    private static Config config;

    private static VocabularyEntry[] nextTerms = null;
    private static long[] vocabularyEntryMemoryOffset = null;

    private static FileChannel[] documentIdChannels = null;

    private static FileChannel[] frequencyChannels = null;
    /**
     * Standard pathname for partial index documents files
     */
    private static String PATH_TO_PARTIAL_DOCID;

    /**
     * Standard pathname for partial index frequencies files
     */
    private static String PATH_TO_PARTIAL_FREQUENCIES;

    /**
     * Standard pathname for partial vocabulary files
     */
    private static String PATH_TO_PARTIAL_VOCABULARY;
    /**
     * Path to the inverted index docs file
     */
    private static String PATH_TO_INVERTED_INDEX_DOCS;

    /**
     * Path to the inverted index freqs file
     */
    private static String PATH_TO_INVERTED_INDEX_FREQS;

    /**
     * Path to vocabulary
     */
    private static String PATH_TO_VOCABULARY;

    /**
     * path to block descriptors file
     */
    private static String PATH_TO_BLOCK_DESCRIPTORS;

    private static String PATH_TO_COLLECTION_STATISTICS;


    public void setupMerger3() {
        PATH_TO_BLOCK_DESCRIPTORS = config.getBlockDescriptorConfig().getBlockDescriptorsPath();
        PATH_TO_VOCABULARY = config.getVocabularyConfig().getVocabularyPath();
        PATH_TO_INVERTED_INDEX_DOCS = config.getInvertedIndexConfig().getInvertedIndexDocs();
        PATH_TO_INVERTED_INDEX_FREQS = config.getInvertedIndexConfig().getInvertedIndexFreqsFile();
        PATH_TO_PARTIAL_VOCABULARY = config.getPartialResultsConfig().getPartialVocabularyDir() + config.getVocabularyConfig().getVocabularyFile();
        PATH_TO_PARTIAL_FREQUENCIES = config.getPartialResultsConfig().getFrequencyDir() + config.getVocabularyConfig().getFrequencyFileName();
        PATH_TO_PARTIAL_DOCID = config.getPartialResultsConfig().getDocIdDir() + config.getVocabularyConfig().getDocIdFileName();
        PATH_TO_COLLECTION_STATISTICS = config.getCollectionConfig().getCollectionStatisticsPath();
    }

    private Merger3(Config config, int numIndexes) {
        Merger3.config = config;
        setupMerger3();
        Merger3.numIndexes = numIndexes;

        nextTerms = new VocabularyEntry[numIndexes];
        vocabularyEntryMemoryOffset = new long[numIndexes];
        documentIdChannels = new FileChannel[numIndexes];
        frequencyChannels = new FileChannel[numIndexes];

        freqsMemoryOffset = 0;
        docsMemoryOffset = 0;

        try {
            for (int i = 0; i < numIndexes; i++) {
                nextTerms[i] = new VocabularyEntry();
                vocabularyEntryMemoryOffset[i] = 0;

                long ret = nextTerms[i].readFromDisk(vocabularyEntryMemoryOffset[i],PATH_TO_PARTIAL_VOCABULARY + "_" + i);
                if (ret == -1 || ret == 0) {
                    nextTerms[i] = null;
                }

                documentIdChannels[i] = (FileChannel) Files.newByteChannel(Paths.get(PATH_TO_PARTIAL_DOCID+ "_" + i),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                frequencyChannels[i] = (FileChannel) Files.newByteChannel(Paths.get(PATH_TO_PARTIAL_FREQUENCIES+ "_" + i),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
            }
        }catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static Merger3 with(Config config, int numIndexes) {
        return new Merger3(config, numIndexes);
    }

    public static boolean mergeIndexes(int numIndexes, boolean compressionMode, boolean debugMode) {

        Merger3.numIndexes = numIndexes;

        long vocabularySize = 0;
        long vocabularyMemoryOffset = 0;

        try (
                FileChannel vocabularyChannel = (FileChannel) Files.newByteChannel(
                        Paths.get(PATH_TO_VOCABULARY),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                FileChannel documentIdChannel = (FileChannel) Files.newByteChannel(
                        Paths.get(PATH_TO_INVERTED_INDEX_DOCS),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                FileChannel frequencyChannel = (FileChannel) Files.newByteChannel(
                        Paths.get(PATH_TO_INVERTED_INDEX_FREQS),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                FileChannel descriptorChannel = (FileChannel) Files.newByteChannel(
                        Paths.get(PATH_TO_BLOCK_DESCRIPTORS),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                ) {

            while (true) {
                String termToProcess = getMinimumTerm();

                if (termToProcess == null)
                    break;

                VocabularyEntry vocabularyEntry = new VocabularyEntry(termToProcess);
                PostingList mergedPostingList = processTerm(termToProcess, vocabularyEntry);

                if(mergedPostingList == null){
                    throw new Exception("ERROR: the merged posting list for the term " + termToProcess + " is null");
                }

                // compute information about block descriptors for the posting list to be written

                vocabularyEntry.computeBlocksInformation();

                // compute maximal number of postings that can be stored in a block
                int maxNumPostings = vocabularyEntry.getMaxNumberOfPostingsInBlock();

                // create iterator over posting list to be written
                Iterator<Posting> plIterator = mergedPostingList.getPostings().iterator();

                int numBlocks = vocabularyEntry.getNumBlocks();

                // save posting list on disk writing each block
                for(int i=0; i< numBlocks; i++){
                    // create a new block descriptor and update its information
                    BlockDescriptor blockDescriptor = new BlockDescriptor();
                    blockDescriptor.setDocidOffset(docsMemoryOffset);
                    blockDescriptor.setFreqOffset(freqsMemoryOffset);

                    // number of postings written in the block
                    int postingsInBlock = 0;

                    int alreadyWrittenPostings = i*maxNumPostings;

                    // number of postings to be written in the current block
                    int nPostingsToBeWritten = (Math.min((mergedPostingList.getPostings().size() - alreadyWrittenPostings), maxNumPostings));

                    if(compressionMode){
                        // arrays where to store docids and frequencies to be written in current block
                        int[] docids = new int[nPostingsToBeWritten];
                        int[] freqs = new int[nPostingsToBeWritten];

                        // initialize docids and freqs arrays
                        while(true){
                            // get next posting to be written to disk
                            Posting currPosting = plIterator.next();
                            docids[postingsInBlock] = currPosting.getDocId();
                            freqs[postingsInBlock] = currPosting.getFrequency();

                            postingsInBlock++;

                            if (postingsInBlock == nPostingsToBeWritten){

                                byte[] compressedDocs = VariableByteCompressor.integerArrayCompression(docids);
                                byte[] compressedFreqs = UnaryCompressor.integerArrayCompression(freqs);

                                try{
                                    // instantiation of MappedByteBuffer for integer list of docids and for integer list of freqs
                                    MappedByteBuffer docsBuffer = documentIdChannel.map(FileChannel.MapMode.READ_WRITE, docsMemoryOffset, compressedDocs.length);
                                    MappedByteBuffer freqsBuffer = frequencyChannel.map(FileChannel.MapMode.READ_WRITE, freqsMemoryOffset, compressedFreqs.length);

                                    // write compressed posting lists to disk
                                    docsBuffer.put(compressedDocs);
                                    freqsBuffer.put(compressedFreqs);

                                    // update the size of the block
                                    blockDescriptor.setDocidSize(compressedDocs.length);
                                    blockDescriptor.setFreqSize(compressedFreqs.length);

                                    // update the max docid of the block
                                    blockDescriptor.setMaxDocid(currPosting.getDocId());

                                    // update the number of postings in the block
                                    blockDescriptor.setNumPostings(postingsInBlock);

                                    // write the block descriptor on disk
                                    blockDescriptor.saveDescriptorOnDisk(descriptorChannel);

                                    docsMemoryOffset+=compressedDocs.length;
                                    freqsMemoryOffset+=compressedFreqs.length;
                                    break;

                                } catch (Exception e) {
                                    cleanUp();
                                    e.printStackTrace();
                                    return false;
                                }
                            }
                        }
                    } else {
                        // posting list must not be compressed

                        // set docs and freqs num bytes as (number of postings)*4
                        blockDescriptor.setDocidSize(nPostingsToBeWritten*4);
                        blockDescriptor.setFreqSize(nPostingsToBeWritten*4);

                        // write postings to block
                        try {
                            // instantiation of MappedByteBuffer for integer list of docids and for integer list of freqs
                            MappedByteBuffer docsBuffer = documentIdChannel.map(FileChannel.MapMode.READ_WRITE, docsMemoryOffset, nPostingsToBeWritten* 4L);
                            MappedByteBuffer freqsBuffer = frequencyChannel.map(FileChannel.MapMode.READ_WRITE, freqsMemoryOffset, nPostingsToBeWritten* 4L);

                            if (docsBuffer != null && freqsBuffer != null) {
                                while (true) {
                                    // get next posting to be written to disk
                                    Posting currPosting = plIterator.next();

                                    // encode docid and freq
                                    docsBuffer.putInt(currPosting.getDocId());
                                    freqsBuffer.putInt(currPosting.getFrequency());

                                    // increment counter of number of postings written in the block
                                    postingsInBlock++;

                                    // check if currPosting is the last posting to be written in the current block
                                    if (postingsInBlock == nPostingsToBeWritten) {
                                        // update the max docid of the block
                                        blockDescriptor.setMaxDocid(currPosting.getDocId());

                                        // update the number of postings in the block
                                        blockDescriptor.setNumPostings(postingsInBlock);

                                        // write the block descriptor on disk
                                        blockDescriptor.saveDescriptorOnDisk(descriptorChannel);
                                        docsMemoryOffset+=nPostingsToBeWritten*4L;
                                        freqsMemoryOffset+=nPostingsToBeWritten*4L;
                                        break;
                                    }
                                }
                            }
                        }
                        catch (Exception e){
                            cleanUp();
                            e.printStackTrace();
                        }
                    }
                }
                // save vocabulary entry on disk
                vocabularyMemoryOffset = vocabularyEntry.writeEntry(vocabularyMemoryOffset, vocabularyChannel);
                vocabularySize++;

                if(debugMode){
                    mergedPostingList.debugSaveToDisk("debugDOCIDS.txt", "debugFREQS.txt", maxNumPostings);
                    vocabularyEntry.debugSaveToDisk("debugVOCABULARY.txt");
                }
            }

            cleanUp();
            DocumentCollectionSize.updateVocabularySize(vocabularySize);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * method to process a term in a parallelized way across all the intermediate indexes:
     * - create the final posting list
     * - create the vocabulary entry for the term
     * - update term statistics in the vocabulary entry (side effect)
     *
     * @param termToProcess: term to be processed
     * @param vocabularyEntry: vocabulary entry for new term
     * @return posting list of the processed term
     */
    private static PostingList processTerm(String termToProcess, VocabularyEntry vocabularyEntry) {
        // new posting list for the term
        PostingList finalList = new PostingList(config);
        finalList.setTerm(termToProcess);

        // processing the term
        for (int i = 0; i < numIndexes; i++) {

            // Found the matching term
            if (nextTerms[i] != null && nextTerms[i].getTerm().equals(termToProcess)) {

                // retrieve posting list from partial inverted index file
                PostingList intermediatePostingList = loadList(nextTerms[i], i);
                if(intermediatePostingList == null)
                    return null;

                // update max docLen
                vocabularyEntry.updateBM25Statistics(nextTerms[i].getBM25Tf(), nextTerms[i].getBM25Dl());

                //update vocabulary statistics
                vocabularyEntry.updateStatistics(intermediatePostingList);

                // Append the posting list to the final posting list of the term
                finalList.appendPostings(intermediatePostingList.getPostings());


            }
        }

        // Update the nextList array with the next term to process
        moveVocabulariesToNextTerm(termToProcess);

        // writing to vocabulary the space occupancy and memory offset of the posting list into
        vocabularyEntry.setDocIdOffset(docsMemoryOffset);
        vocabularyEntry.setFrequencyOffset(freqsMemoryOffset);


        // compute the final idf
        vocabularyEntry.computeBlocksInformation();
        // compute the term upper bounds
        vocabularyEntry.computeUpperBounds();

        return finalList;
    }

    /**
     * Method to read the next term in these vocabularies in which we had the last processed term
     * @param processedTerm: last processed term, it is used to find which vocabularies must be read
     */
    private static void moveVocabulariesToNextTerm(String processedTerm) {

        // for each intermediate vocabulary
        for(int i=0; i<numIndexes; i++){
            // check if the last processed term was present in the i-th vocabulary
            if(nextTerms[i] != null && nextTerms[i].getTerm().equals(processedTerm)) {
                // last processed term was present

                // update next memory offset to be read from the i-th vocabulary
                vocabularyEntryMemoryOffset[i] += VocabularyEntry.ENTRY_SIZE;

                // read next vocabulary entry from the i-th vocabulary
                long ret = nextTerms[i].readFromDisk(vocabularyEntryMemoryOffset[i], PATH_TO_PARTIAL_VOCABULARY+ "_" +i);

                // check if errors occurred while reading the vocabulary entry
                if(ret == -1 || ret == 0){
                    // read ended or an error occurred
                    nextTerms[i] = null;
                }
            }
        }
    }

    /**
     * loads a partial posting list
     *
     * @param term  the term of the partial posting list
     * @param index the partial index from which the list is read
     * @return the partial posting list
     */
    private static PostingList loadList(VocabularyEntry term, int index) {
        PostingList newList;

        try {
            // instantiation of MappedByteBuffer for integer list of docids
            MappedByteBuffer docBuffer = documentIdChannels[index].map(
                    FileChannel.MapMode.READ_ONLY,
                    term.getDocIdOffset(),
                    term.getDocIdSize()
            );

            // instantiation of MappedByteBuffer for integer list of frequencies
            MappedByteBuffer freqBuffer = frequencyChannels[index].map(
                    FileChannel.MapMode.READ_ONLY,
                    term.getFrequencyOffset(),
                    term.getFrequencySize()
            );

            // create the posting list for the term
            newList = new PostingList(config, term.getTerm());

            for (int i = 0; i < term.getDocumentFrequency(); i++) {
                Posting posting = new Posting(docBuffer.getInt(), freqBuffer.getInt());
                newList.getPostings().add(posting);
            }
            return newList;
        } catch (Exception e) {
            cleanup();
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the minimum term of the terms to be processed in the intermediate indexes
     * @return the next term to process
     */
    public static String getMinimumTerm() {
        String term = null;

        for (int i = 0; i < numIndexes; i++) {
            // check if there are still posting lists to be processed at intermediate index 'i'
            if (nextTerms[i] == null)
                continue;

            // next term to be processed at the intermediate index 'i'
            String nextTerm = nextTerms[i].getTerm();

            if (term == null) {
                term = nextTerm;
                continue;
            }

            if (nextTerm.compareTo(term) < 0)
                term = nextTerm;
        }
        return term;
    }

    private static void cleanup() {
        try {
            for (int i = 0; i < numIndexes; i++) {
                if (documentIdChannels[i] != null)
                    documentIdChannels[i].close();
                if (frequencyChannels[i] != null)
                    frequencyChannels[i].close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public VocabularyEntry getNextTerms(int i) {
        return nextTerms[i];
    }

    /**
     * method to clean up the files:
     * - remove partial indexes
     * - remove partial vocabularies
     */
    private static void cleanUp() {
        try{
            for(int i = 0; i < numIndexes; i++){
                if(documentIdChannels[i] != null){
                    documentIdChannels[i].close();
                }
                if (frequencyChannels[i] != null) {
                    frequencyChannels[i].close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * method to print how much space does the inverted index takes in disk.
     * This method is needed to compute performance statistics.
     */
    public static void printPerformanceStatistics(){
        System.out.println("Inverted index's memory occupancy:");
        System.out.println("\t> docids: "+docsMemoryOffset+ "bytes");
        System.out.println("\t> freqs: "+freqsMemoryOffset+ "bytes");
    }

    /**
     * needed for testing purposes
     * @param pathToVocabulary: path to be set as vocabulary path
     */
    public static void setPathToVocabulary(String pathToVocabulary) {
        PATH_TO_VOCABULARY = pathToVocabulary;
    }

    /**
     * needed for testing purposes
     * @param pathToInvertedIndexDocs: path to be set as inverted index's docs path
     */
    public static void setPathToInvertedIndexDocs(String pathToInvertedIndexDocs) {
        PATH_TO_INVERTED_INDEX_DOCS = pathToInvertedIndexDocs;
    }

    /**
     * needed for testing purposes
     * @param invertedIndexFreqs: path to be set as inverted index's freqs path
     */
    public static void setPathToInvertedIndexFreqs(String invertedIndexFreqs) { PATH_TO_INVERTED_INDEX_FREQS = invertedIndexFreqs;}

    /**
     * needed for testing purposes
     * @param blockDescriptorsPath: path to be set as block descriptors' path
     */
    public static void setPathToBlockDescriptors(String blockDescriptorsPath) { PATH_TO_BLOCK_DESCRIPTORS = blockDescriptorsPath;}

    /**
     * needed for testing purposes
     * @param pathToPartialIndexesDocs: path to be set
     */
    public static void setPathToPartialIndexesDocs(String pathToPartialIndexesDocs) { PATH_TO_PARTIAL_DOCID = pathToPartialIndexesDocs;}

    /**
     * needed for testing purposes
     * @param pathToPartialIndexesFreqs: path to be set
     */
    public static void setPathToPartialIndexesFreqs(String pathToPartialIndexesFreqs) { PATH_TO_PARTIAL_FREQUENCIES = pathToPartialIndexesFreqs;}

    /**
     * needed for testing purposes
     * @param pathToPartialVocabularies: path to be set
     */
    public static void setPathToPartialVocabularies(String pathToPartialVocabularies) { PATH_TO_PARTIAL_VOCABULARY = pathToPartialVocabularies;}



}

package it.unipi.dii.aide.mircv;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.data.BlockDescriptor;
import it.unipi.dii.aide.mircv.data.VocabularyEntry;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;


//import it.unipi.dii.aide.mircv.common.beans.BlockDescriptor;
//import it.unipi.dii.aide.mircv.Posting;
//import it.unipi.dii.aide.mircv.PostingList;
//import it.unipi.dii.aide.mircv.common.beans.VocabularyEntry;
//import it.unipi.dii.aide.mircv.common.compression.UnaryCompressor;
//import it.unipi.dii.aide.mircv.common.compression.VariableByteCompressor;
//import it.unipi.dii.aide.mircv.common.config.CollectionSize;
//import it.unipi.dii.aide.mircv.common.config.ConfigurationParameters;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;


public class Merger {

    // Inverted index's next free memory offset in docids file
    private static long docsMemOffset;

    // Inverted index's next free memory offset in freqs file
    private static long freqsMemOffset;

    // Number of intermediate indexes produced by SPIMI algorithm
    private static int numIndexes;

    // Standard pathname for partial index documents files
    private static String PATH_TO_PARTIAL_INDEXES_DOCS;

    // Standard pathname for partial index frequencies files
    private static String PATH_TO_PARTIAL_INDEXES_FREQS;

    // Standard pathname for partial vocabulary files
    private static String PATH_TO_PARTIAL_VOCABULARIES;

    // Path to the inverted index docs file
    private static String PATH_TO_INVERTED_INDEX_DOCS;

    // Path to the inverted index freqs file
    private static String PATH_TO_INVERTED_INDEX_FREQS;

    // Path to vocabulary
    private static String PATH_TO_VOCABULARY;

    // Path to block descriptors file
    private static String PATH_TO_BLOCK_DESCRIPTORS;

    // Array used to point to the next vocabulary entry to process for each partial index
    private static VocabularyEntry[] nextTerms;

    // Memory offsets of the last read vocabulary entry
    private static long[] vocEntryMemOffset;

    // File channels for docids of partial indexes
    private static FileChannel[] docidChannels;

    // File channels for frequencies of partial indexes
    private static FileChannel[] frequencyChannels;

    private static Config config;

    private static boolean initialize(Config configuration) {
        // Initialize arrays and offsets
        config = configuration;
        nextTerms = new VocabularyEntry[numIndexes];
        vocEntryMemOffset = new long[numIndexes];
        docidChannels = new FileChannel[numIndexes];
        frequencyChannels = new FileChannel[numIndexes];
        freqsMemOffset = 0;
        docsMemOffset = 0;

        try {
            // Initialize data structures for each index
            for (int i = 0; i < numIndexes; i++) {
                nextTerms[i] = new VocabularyEntry(config.blockDescriptorsPath);
                vocEntryMemOffset[i] = 0;

                // Read the first entry of the vocabulary from disk
                long ret = nextTerms[i].readFromDisk(vocEntryMemOffset[i], getPathToPartialVocabularies(i));

                // Check for errors or end of file during vocabulary entry reading
                if (ret == -1 || ret == 0) {
                    nextTerms[i] = null;
                }

                // Open file channels for docids and frequencies of the partial index
                docidChannels[i] = openFileChannel(getPathToPartialIndexesDocs(i));
                frequencyChannels[i] = openFileChannel(getPathToPartialIndexesFreqs(i));
            }
            return true; // Initialization successful
        } catch (IOException e) {
            // Clean up in case of exception
            cleanUp();
            e.printStackTrace();
            return false; // Initialization failed
        }
    }


    /**
     * Opens a FileChannel for a given file path with specified options.
     *
     * @param filePath The path of the file for which the FileChannel is to be opened.
     * @return The opened FileChannel.
     * @throws IOException If an I/O error occurs while opening the FileChannel.
     */
    private static FileChannel openFileChannel(String filePath) throws IOException {
        // Open a FileChannel for the specified file path with write, read, and create options
        return (FileChannel) Files.newByteChannel(
                Paths.get(filePath),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE
        );
    }


    /**
     * Constructs the file path for the docids of a partial index.
     *
     * @param index The index of the partial index.
     * @return The constructed file path.
     */
    private static String getPathToPartialIndexesDocs(int index) {
        return ConfigurationParameters.getDocidsDir() + ConfigurationParameters.getDocidsFileName() + "_" + index;
    }

    /**
     * Constructs the file path for the frequencies of a partial index.
     *
     * @param index The index of the partial index.
     * @return The constructed file path.
     */
    private static String getPathToPartialIndexesFreqs(int index) {
        return ConfigurationParameters.getFrequencyDir() + ConfigurationParameters.getFrequencyFileName() + "_" + index;
    }

    /**
     * Constructs the file path for the vocabulary of a partial index.
     *
     * @param index The index of the partial index.
     * @return The constructed file path.
     */
    private static String getPathToPartialVocabularies(int index) {
        return ConfigurationParameters.getPartialVocabularyDir() + ConfigurationParameters.getVocabularyFileName() + "_" + index;
    }


    private static String getPathToVocabulary() {
        return PATH_TO_VOCABULARY;
    }

    private static String getPathToInvertedIndexDocs() {
        return PATH_TO_INVERTED_INDEX_DOCS;
    }

    private static String getPathToInvertedIndexFreqs() {
        return PATH_TO_INVERTED_INDEX_FREQS;
    }

    private static String getPathToBlockDescriptors() {
        return PATH_TO_BLOCK_DESCRIPTORS;
    }

    private static String getPathToPartialIndexesDocs() {
        return PATH_TO_PARTIAL_INDEXES_DOCS;
    }

    private static String getPathToPartialIndexesFreqs() {
        return PATH_TO_PARTIAL_INDEXES_FREQS;
    }

    private static String getPathToPartialVocabularies() {
        return PATH_TO_PARTIAL_VOCABULARIES;
    }

    /**
     * The effective merging pipeline:
     * - finds the minimum term between the indexes
     * - creates the whole posting list and the vocabulary entry for that term
     * - stores them in memory
     *
     * @param numIndexes      Number of partial vocabularies and partial indexes created.
     * @param compressionMode Flag deciding whether to compress posting lists or not.
     * @param debugMode       Flag to enable debugging information.
     * @return True if the merging is complete, false otherwise.
     */
    public static boolean mergeIndexes(int numIndexes, boolean compressionMode, boolean debugMode) {
        // Initialization operations
        if (!initialize()) {
            return false;
        }

        // Size of the vocabulary
        long vocSize = 0;

        // Next memory offset where to write the next vocabulary entry
        long vocMemOffset = 0;

        try (FileChannel vocabularyChan = openFileChannel(getPathToVocabulary());
             FileChannel docidChan = openFileChannel(getPathToInvertedIndexDocs());
             FileChannel frequencyChan = openFileChannel(getPathToInvertedIndexFreqs());
             FileChannel descriptorChan = openFileChannel(getPathToBlockDescriptors())) {

            // Process terms and merge posting lists until there are no more terms
            while (true) {
                // Find next term to be processed (the minimum in lexicographical order)
                String termToProcess = getMinTerm();

                if (termToProcess == null) {
                    break;
                }

                // New vocabulary entry for the processed term
                VocabularyEntry vocabularyEntry = new VocabularyEntry(termToProcess, config.blockDescriptorsPath);

                // Merge the posting lists for the term to be processed
                PostingList mergedPostingList = processTerm(termToProcess, vocabularyEntry);

                if (mergedPostingList == null) {
                    throw new Exception("ERROR: the merged posting list for the term " + termToProcess + " is null");
                }

                // Compute information about block descriptors for the posting list to be written
                vocabularyEntry.computeBlocksInformation();

                // Compute maximal number of postings that can be stored in a block
                int maxNumPostings = vocabularyEntry.getMaxNumberOfPostingsInBlock();

                // Create iterator over posting list to be written
                Iterator<Posting> plIterator = mergedPostingList.getPostings().iterator();
                int numBlocks = vocabularyEntry.getNumBlocks();

                // Save posting list on disk writing each block
                for (int i = 0; i < numBlocks; i++) {
                    // Create a new block descriptor and update its information
                    BlockDescriptor blockDescriptor = new BlockDescriptor();
                    blockDescriptor.setDocidOffset(docsMemOffset);
                    blockDescriptor.setFreqOffset(freqsMemOffset);

                    // Number of postings written in the block
                    int postingsInBlock = 0;

                    int alreadyWrittenPostings = i * maxNumPostings;

                    // Number of postings to be written in the current block
                    int nPostingsToBeWritten = Math.min((mergedPostingList.getPostings().size() - alreadyWrittenPostings), maxNumPostings);

                    // Process posting list based on compression mode
                    if (compressionMode) {
                        processCompressedPostingList(plIterator, blockDescriptor, nPostingsToBeWritten, maxNumPostings, docidChan, frequencyChan, descriptorChan);
                    } else {
                        processUncompressedPostingList(plIterator, blockDescriptor, nPostingsToBeWritten, docidChan, frequencyChan, descriptorChan);
                    }

                    // Increment the vocabulary memory offset
                    vocMemOffset = vocabularyEntry.writeEntryToDisk(vocMemOffset, vocabularyChan);
                    vocSize++;

                    // Debugging information
                    if (debugMode) {
                        mergedPostingList.debugSaveToDisk("debugDOCIDS.txt", "debugFREQS.txt", maxNumPostings);
                        vocabularyEntry.debugSaveToDisk("debugVOCABULARY.txt");
                    }
                }
            }

            // Clean up resources
            cleanUp();

            // Update vocabulary size
            CollectionSize.updateVocabularySize(vocSize);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Process compressed posting list and write to disk.
     */
    private static void processCompressedPostingList(Iterator<Posting> plIterator, BlockDescriptor blockDescriptor,
                                                     int nPostingsToBeWritten, int maxNumPostings,
                                                     FileChannel docidChan, FileChannel frequencyChan,
                                                     FileChannel descriptorChan) throws IOException {
        int[] docids = new int[nPostingsToBeWritten];
        int[] freqs = new int[nPostingsToBeWritten];

        // Initialize docids and freqs arrays
        while (plIterator.hasNext()) {
            Posting currPosting = plIterator.next();
            docids[postingsInBlock] = currPosting.getDocId();
            freqs[postingsInBlock] = currPosting.getFrequency();

            postingsInBlock++;

            if (postingsInBlock == nPostingsToBeWritten) {
                byte[] compressedDocs = VariableByteCompressor.integerArrayCompression(docids);
                byte[] compressedFreqs = UnaryCompressor.integerArrayCompression(freqs);

                // Write compressed posting lists to disk
                writeCompressedPostingListsToDisk(docidChan, frequencyChan, descriptorChan,
                        compressedDocs, compressedFreqs, blockDescriptor, maxNumPostings);

                break;
            }
        }
    }

    /**
     * Process uncompressed posting list and write to disk.
     */
    private static void processUncompressedPostingList(Iterator<Posting> plIterator, BlockDescriptor blockDescriptor,
                                                       int nPostingsToBeWritten, FileChannel docidChan,
                                                       FileChannel frequencyChan, FileChannel descriptorChan) throws IOException {
        // Posting list must not be compressed

        // Set docs and freqs num bytes as (number of postings)*4
        blockDescriptor.setDocidSize(nPostingsToBeWritten * 4);
        blockDescriptor.setFreqSize(nPostingsToBeWritten * 4);

        // Write postings to block
        while (plIterator.hasNext()) {
            Posting currPosting = plIterator.next();

            // Encode docid and freq
            docsBuffer.putInt(currPosting.getDocId());
            freqsBuffer.putInt(currPosting.getFrequency());

            postingsInBlock++;

            if (postingsInBlock == nPostingsToBeWritten) {
                // Update the max docid of the block
                blockDescriptor.setMaxDocid(currPosting.getDocId());

                // Update the number of postings in the block
                blockDescriptor.setNumPostings(postingsInBlock);

                // Write the block descriptor on disk
                blockDescriptor.saveDescriptorOnDisk(descriptorChan);

                docsMemOffset += nPostingsToBeWritten * 4L;
                freqsMemOffset += nPostingsToBeWritten * 4L;
                break;
            }
        }
    }

    /**
     * Write compressed posting lists to disk.
     */
    private static void writeCompressedPostingListsToDisk(FileChannel docidChan, FileChannel frequencyChan,
                                                          FileChannel descriptorChan, byte[] compressedDocs,
                                                          byte[] compressedFreqs, BlockDescriptor blockDescriptor,
                                                          int maxNumPostings) throws IOException {
        try {
            // Instantiation of MappedByteBuffer for integer list of docids and for integer list of freqs
            MappedByteBuffer docsBuffer = docidChan.map(FileChannel.MapMode.READ_WRITE, docsMemOffset, compressedDocs.length);
            MappedByteBuffer freqsBuffer = frequencyChan.map(FileChannel.MapMode.READ_WRITE, freqsMemOffset, compressedFreqs.length);

            // Write compressed posting lists to disk
            docsBuffer.put(compressedDocs);
            freqsBuffer.put(compressedFreqs);

            // Update the size of the block
            blockDescriptor.setDocidSize(compressedDocs.length);
            blockDescriptor.setFreqSize(compressedFreqs.length);

            // Update the max docid of the block
            blockDescriptor.setMaxDocid(currPosting.getDocid());

            // Update the number of postings in the block
            blockDescriptor.setNumPostings(postingsInBlock);

            // Write the block descriptor on disk
            blockDescriptor.saveDescriptorOnDisk(descriptorChan);

            docsMemOffset += compressedDocs.length;
            freqsMemOffset += compressedFreqs.length;
        } catch (Exception e) {
            cleanUp();
            e.printStackTrace();
            //return false;
        }
    }

    /**
     * Returns the minimum term among the terms to be processed in the intermediate indexes.
     *
     * @return The next term to process
     */
    private static String getMinTerm() {
        String minTerm = null;

        // Iterate over each intermediate index
        for (int i = 0; i < numIndexes; i++) {
            String currentTerm = getNextTermToProcess(i);

            // Update the minimum term
            if (minTerm == null || currentTerm.compareTo(minTerm) < 0) {
                minTerm = currentTerm;
            }
        }
        return minTerm;
    }

    /**
     * Gets the next term to be processed at the given intermediate index.
     *
     * @param index The intermediate index
     * @return The next term to process
     */
    private static String getNextTermToProcess(int index) {
        if (nextTerms[index] == null) {
            return null; // No more terms to process in this index
        }

        return nextTerms[index].getTerm();
    }

    /**
     * Cleans up resources by closing file channels for docids and frequencies of partial indexes.
     */
    private static void cleanUp() {
        try {
            // Close file channels for docids and frequencies of each partial index
            for (int i = 0; i < numIndexes; i++) {
                if (docidChannels[i] != null) {
                    docidChannels[i].close();
                }
                if (frequencyChannels[i] != null) {
                    frequencyChannels[i].close();
                }
            }
        } catch (Exception e) {
            // Print stack trace in case of an exception during cleanup
            e.printStackTrace();
        }
    }

    /**
     * Loads a partial posting list from disk based on the given term and index.
     *
     * @param term  The term of the partial posting list.
     * @param index The partial index from which the list is read.
     * @return The partial posting list loaded from disk.
     */
    private static PostingList loadList(VocabularyEntry term, int index) {
        PostingList newList;

        try {
            // Instantiate MappedByteBuffer for the integer list of docids
            MappedByteBuffer docBuffer = docidChannels[index].map(
                    FileChannel.MapMode.READ_ONLY,
                    term.getDocidOffset(),
                    term.getDocidSize()
            );

            // Instantiate MappedByteBuffer for the integer list of frequencies
            MappedByteBuffer freqBuffer = frequencyChannels[index].map(
                    FileChannel.MapMode.READ_ONLY,
                    term.getFrequencyOffset(),
                    term.getFrequencySize()
            );

            // Create the posting list for the term
            newList = new PostingList(term.getTerm());

            // Iterate over the docids and frequencies to construct the posting list
            for (int i = 0; i < term.getDf(); i++) {
                Posting posting = new Posting(docBuffer.getInt(), freqBuffer.getInt());
                newList.getPostings().add(posting);
            }
            return newList;
        } catch (Exception e) {
            // Clean up and print stack trace in case of an exception
            cleanUp();
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Processes a term in a parallelized way across all the intermediate indexes:
     * - Creates the final posting list
     * - Creates the vocabulary entry for the term
     * - Updates term statistics in the vocabulary entry (side effect)
     *
     * @param termToProcess   Term to be processed
     * @param vocabularyEntry Vocabulary entry for the new term
     * @return Posting list of the processed term
     */
    private static PostingList processTerm(String termToProcess, VocabularyEntry vocabularyEntry) {
        // Initialize a new posting list for the term
        PostingList finalList = new PostingList();
        finalList.setTerm(termToProcess);

        // Process the term in each intermediate index
        for (int i = 0; i < numIndexes; i++) {
            // Check if the term matches in the current intermediate index
            if (isMatchingTerm(nextTerms[i], termToProcess)) {
                // Retrieve posting list from the partial inverted index file
                PostingList intermediatePostingList = loadList(nextTerms[i], i);

                // Handle null posting list
                if (intermediatePostingList == null) {
                    return null;
                }

                // Update vocabulary statistics based on the current intermediate index
                updateVocabularyStatistics(vocabularyEntry, nextTerms[i], intermediatePostingList);

                // Append the posting list to the final posting list of the term
                finalList.appendPostings(intermediatePostingList.getPostings());
            }
        }

        // Move to the next term in each intermediate vocabulary
        moveVocabulariesToNextTerm(termToProcess);

        // Update vocabulary entry information
        updateVocabularyEntryInfo(vocabularyEntry);

        return finalList;
    }

    /**
     * Checks if the given vocabulary entry's term matches the term to be processed.
     *
     * @param entry           Vocabulary entry to check
     * @param termToProcess   Term to be processed
     * @return True if the terms match, false otherwise
     */
    private static boolean isMatchingTerm(VocabularyEntry entry, String termToProcess) {
        return entry != null && entry.getTerm().equals(termToProcess);
    }

    /**
     * Updates the vocabulary entry statistics based on the current intermediate index.
     *
     * @param vocabularyEntry           Vocabulary entry to be updated
     * @param entry                     Current intermediate index's vocabulary entry
     * @param intermediatePostingList   Posting list from the current intermediate index
     */
    private static void updateVocabularyStatistics(VocabularyEntry vocabularyEntry, VocabularyEntry entry, PostingList intermediatePostingList) {
        vocabularyEntry.updateBM25Statistics(entry.getBM25Tf(), entry.getBM25Dl());
        vocabularyEntry.updateStatistics(intermediatePostingList);
    }

    /**
     * Updates the vocabulary entry information after processing the term.
     *
     * @param vocabularyEntry Vocabulary entry to be updated
     */
    private static void updateVocabularyEntryInfo(VocabularyEntry vocabularyEntry) {
        // Set memory offsets and compute additional information
        vocabularyEntry.setDocidOffset(docsMemOffset);
        vocabularyEntry.setFrequencyOffset(freqsMemOffset);
        vocabularyEntry.calculateInverseDocumentFrequency();
        vocabularyEntry.computeUpperBounds();
    }

    /**
     * Moves to the next term in each intermediate vocabulary where the last processed term was present.
     *
     * @param processedTerm Last processed term
     */
    private static void moveVocabulariesToNextTerm(String processedTerm) {
        // Iterate over each intermediate vocabulary
        for (int i = 0; i < numIndexes; i++) {
            // Check if the last processed term was present in the i-th vocabulary
            if (isMatchingTerm(nextTerms[i], processedTerm)) {
                // Update next memory offset to be read from the i-th vocabulary
                vocEntryMemOffset[i] += VocabularyEntry.ENTRY_SIZE;

                // Read next vocabulary entry from the i-th vocabulary
                long ret = nextTerms[i].readFromDisk(vocEntryMemOffset[i], getPartialVocabularyPath(i));

                // Check if errors occurred while reading the vocabulary entry
                if (ret == -1 || ret == 0) {
                    // Read ended or an error occurred, set the vocabulary entry to null
                    nextTerms[i] = null;
                }
            }
        }
    }


    /**
     * Prints performance statistics related to the inverted index's memory occupancy.
     * This method is needed to compute performance statistics.
     */
    public static void printPerformanceStatistics() {
        System.out.println("Inverted index's memory occupancy:");
        System.out.println("\t> docids: " + docsMemOffset + " bytes");
        System.out.println("\t> freqs: " + freqsMemOffset + " bytes");
    }



    // Setters for testing...

    /**
     * Setter for the path to the vocabulary for testing purposes.
     *
     * @param pathToVocabulary The path to be set as the vocabulary path.
     */
    public static void setPathToVocabulary(String pathToVocabulary) {
        PATH_TO_VOCABULARY = pathToVocabulary;
    }

    /**
     * Setter for the path to the inverted index's docs for testing purposes.
     *
     * @param pathToInvertedIndexDocs The path to be set as the inverted index's docs path.
     */
    public static void setPathToInvertedIndexDocs(String pathToInvertedIndexDocs) {
        PATH_TO_INVERTED_INDEX_DOCS = pathToInvertedIndexDocs;
    }

    /**
     * Setter for the path to the inverted index's freqs for testing purposes.
     *
     * @param invertedIndexFreqs The path to be set as the inverted index's freqs path.
     */
    public static void setPathToInvertedIndexFreqs(String invertedIndexFreqs) {
        PATH_TO_INVERTED_INDEX_FREQS = invertedIndexFreqs;
    }

    /**
     * Setter for the path to block descriptors for testing purposes.
     *
     * @param blockDescriptorsPath The path to be set as block descriptors' path.
     */
    public static void setPathToBlockDescriptors(String blockDescriptorsPath) {
        PATH_TO_BLOCK_DESCRIPTORS = blockDescriptorsPath;
    }

    /**
     * Setter for the path to partial indexes docs for testing purposes.
     *
     * @param pathToPartialIndexesDocs The path to be set for partial indexes docs.
     */
    public static void setPathToPartialIndexesDocs(String pathToPartialIndexesDocs) {
        PATH_TO_PARTIAL_INDEXES_DOCS = pathToPartialIndexesDocs;
    }

    /**
     * Setter for the path to partial indexes freqs for testing purposes.
     *
     * @param pathToPartialIndexesFreqs The path to be set for partial indexes freqs.
     */
    public static void setPathToPartialIndexesFreqs(String pathToPartialIndexesFreqs) {
        PATH_TO_PARTIAL_INDEXES_FREQS = pathToPartialIndexesFreqs;
    }

    /**
     * Setter for the path to partial vocabularies for testing purposes.
     *
     * @param pathToPartialVocabularies The path to be set for partial vocabularies.
     */
    public static void setPathToPartialVocabularies(String pathToPartialVocabularies) {
        PATH_TO_PARTIAL_VOCABULARIES = pathToPartialVocabularies;
    }


}

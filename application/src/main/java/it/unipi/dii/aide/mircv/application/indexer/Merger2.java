package it.unipi.dii.aide.mircv.application.indexer;

import it.unipi.dii.aide.mircv.application.compression.UnaryCompressor;
import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.*;
import it.unipi.dii.aide.mircv.application.compression.VariableByteCompressor;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.nio.MappedByteBuffer;

public class Merger2 {

    private static VocabularyEntry[] nextTerms;
    private final long[] vocabularyEntryMemoryOffset;
    private static Merger2 instance = null;

    private int docsMemOffset;
    private int freqsMemOffset;

    private final Config config;
    private final MergerLoader loader;

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


    public void setupMerger2() {
        PATH_TO_BLOCK_DESCRIPTORS = config.getBlockDescriptorConfig().getBlockDescriptorsPath();
        PATH_TO_VOCABULARY = config.getVocabularyConfig().getVocabularyPath();
        PATH_TO_INVERTED_INDEX_DOCS = config.getInvertedIndexConfig().getInvertedIndexDocs();
        PATH_TO_INVERTED_INDEX_FREQS = config.getInvertedIndexConfig().getInvertedIndexFreqsFile();
        PATH_TO_PARTIAL_VOCABULARY = config.getPartialResultsConfig().getPartialVocabularyDir() + config.getVocabularyConfig().getVocabularyFile();
        PATH_TO_PARTIAL_FREQUENCIES = config.getPartialResultsConfig().getFrequencyDir() + config.getVocabularyConfig().getFrequencyFileName();
        PATH_TO_PARTIAL_DOCID = config.getPartialResultsConfig().getDocIdDir() + config.getVocabularyConfig().getDocIdFileName();
        PATH_TO_COLLECTION_STATISTICS = config.getCollectionConfig().getCollectionStatisticsPath();
    }

    private Merger2(Config config, int numIndexes) {
        this.config = config;
        //setupMerger2();
        this.loader = new MergerLoader(config, numIndexes);

        nextTerms = new VocabularyEntry[numIndexes];
        vocabularyEntryMemoryOffset = new long[numIndexes];

        freqsMemOffset = 0;
        docsMemOffset = 0;

        try {
            for (int i = 0; i < numIndexes; i++) {
                nextTerms[i] = new VocabularyEntry();
                vocabularyEntryMemoryOffset[i] = 0;

                long ret = nextTerms[i].readFromDisk(vocabularyEntryMemoryOffset[i],PATH_TO_PARTIAL_VOCABULARY + "_" + i);
                if (ret == -1 || ret == 0) {
                    nextTerms[i] = null;
                }

                loader.pushDocumentIdChannel(i,  (FileChannel) Files.newByteChannel(Paths.get(PATH_TO_PARTIAL_DOCID+ "_" + i),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE));
                loader.pushFrequencyChannel(i,(FileChannel) Files.newByteChannel(Paths.get(PATH_TO_PARTIAL_FREQUENCIES+ "_" + i),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE));
            }
        } catch (Exception e) {
            loader.cleanup();
            e.printStackTrace();
        }
    }

    public boolean mergeIndexes(int numIndexes,
                                boolean compressionMode,
                                boolean debugMode) {

        long vocabularySize = 0;
        long vocabularyMemoryOffset = 0;

        // open file channels for vocabulary writes, docid and frequency writes, and block descriptor writes
        try (
                FileChannel vocabularyChannel =
                        (FileChannel) Files.newByteChannel(
                                Paths.get(PATH_TO_VOCABULARY),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE);
                FileChannel documentIdChannel =
                        (FileChannel) Files.newByteChannel(
                                Paths.get(PATH_TO_INVERTED_INDEX_DOCS),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE);
                FileChannel frequencyChan =
                        (FileChannel) Files.newByteChannel(
                                Paths.get(PATH_TO_INVERTED_INDEX_FREQS),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE);
                FileChannel descriptorChan =
                        (FileChannel) Files.newByteChannel(
                                Paths.get(PATH_TO_BLOCK_DESCRIPTORS),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE);
        ) {

            MergerWorker worker = MergerWorker.with(config, numIndexes, nextTerms);
            while(true) {
                String termToProcess = worker.getMinimumTerm();

                if (termToProcess == null)
                    break;
                //TODO Verify vocabulary constructor
                VocabularyEntry vocabularyEntry = new VocabularyEntry(termToProcess, PATH_TO_BLOCK_DESCRIPTORS);
                PostingList mergedPostingList = worker.processTerm(loader, vocabularyEntry, termToProcess);

                if(mergedPostingList == null){
                    throw new Exception("ERROR: the merged posting list for the term " + termToProcess + " is null");
                }

                vocabularyEntry.computeBlocksInformation();
                int maxNumPostings = vocabularyEntry.getMaxNumberOfPostingsInBlock();

                // TODO: spostare in processCompressedPostingList
                Iterator<Posting> plIterator = mergedPostingList.getPostings().iterator();
                int numBlocks = vocabularyEntry.getNumBlocks();

                for(int i=0; i< numBlocks; i++) {
                    BlockDescriptor blockDescriptor = new BlockDescriptor();
                    blockDescriptor.setDocidOffset(docsMemOffset);
                    blockDescriptor.setFreqOffset(freqsMemOffset);

                    int nPostingsToBeWritten = getnPostingsToBeWritten(i, maxNumPostings, mergedPostingList);

                    if (compressionMode) {
                        processCompressedPostingList(plIterator, blockDescriptor, nPostingsToBeWritten, maxNumPostings, documentIdChannel, frequencyChan, descriptorChan);
                        loader.cleanup();
                    }else {
                        processUncompressedPostingList(plIterator, blockDescriptor, nPostingsToBeWritten, documentIdChannel, frequencyChan, descriptorChan);
                        loader.cleanup();
                    }
                }

                vocabularyMemoryOffset = vocabularyEntry.writeEntry(vocabularyMemoryOffset, vocabularyChannel);
                vocabularySize++;

                if(debugMode){
                    mergedPostingList.debugSaveToDisk("debugDOCIDS.txt", "debugFREQS.txt", maxNumPostings);
                    vocabularyEntry.debugSaveToDisk("debugVOCABULARY.txt");
                }
            }

            loader.cleanup(); //OCCHIOOOOOOO
            //DocumentCollectionSize.updateVocabularySize(vocabularySize, PATH_TO_COLLECTION_STATISTICS);
            //DocumentCollectionSize.updateVocabularySize(vocabularySize, "../test/data/merger/collection_statistics"); //TESTING OCCCHIOOOOOOOOOOOOOOOOOOOOOOOOOOOOO TOLGLIERE COMMENTO

        } catch (Exception ex) {
            loader.cleanup();
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    private static int getnPostingsToBeWritten(int i, int maxNumPostings, PostingList mergedPostingList) {
        int alreadyWrittenPostings = i * maxNumPostings;
        return (Math.min((mergedPostingList.getPostings().size() - alreadyWrittenPostings), maxNumPostings));
    }

    private void processCompressedPostingList(
            Iterator<Posting> plIterator,
            BlockDescriptor blockDescriptor,
            int nPostingsToBeWritten,
            int maxNumPostings,
            FileChannel docidChan,
            FileChannel frequencyChan,
            FileChannel descriptorChan
    )
            throws IOException {

        int postingsInBlock = 0;
        int[] docids = new int[nPostingsToBeWritten];
        int[] freqs = new int[nPostingsToBeWritten];

        // Initialize docids and freqs arrays
        while (plIterator.hasNext()) { //while (plIterator.hasNext()) {
            Posting currPosting = plIterator.next();
            docids[postingsInBlock] = currPosting.getDocId();
            freqs[postingsInBlock] = currPosting.getFrequency();

            postingsInBlock++;

            if (postingsInBlock == nPostingsToBeWritten) {

                byte[] compressedDocs = VariableByteCompressor.integerArrayCompression(docids);
                byte[] compressedFreqs = UnaryCompressor.integerArrayCompression(freqs);

                // Write compressed posting lists to disk
                this.loader.writeCompressedPostingListsToDisk(currPosting, docidChan, frequencyChan, descriptorChan,
                        compressedDocs, compressedFreqs, blockDescriptor, docsMemOffset, freqsMemOffset, maxNumPostings);

                docsMemOffset += compressedDocs.length;
                freqsMemOffset += compressedFreqs.length;
                break;


            }
        }
    }

    /**
     * Process uncompressed posting list and write to disk.
     */
    private void processUncompressedPostingList(Iterator<Posting> plIterator, BlockDescriptor blockDescriptor,
                                                       int nPostingsToBeWritten, FileChannel docidChan,
                                                       FileChannel frequencyChan, FileChannel descriptorChan) throws IOException {
        int postingsInBlock = 0;

        MappedByteBuffer docsBuffer = docidChan.map(FileChannel.MapMode.READ_WRITE, docsMemOffset, nPostingsToBeWritten* 4L);
        MappedByteBuffer freqsBuffer = frequencyChan.map(FileChannel.MapMode.READ_WRITE, freqsMemOffset, nPostingsToBeWritten* 4L);

        // Posting list must not be compressed
        // Set docs and freqs num bytes as (number of postings)*4
        blockDescriptor.setDocidSize(nPostingsToBeWritten * 4);
        blockDescriptor.setFreqSize(nPostingsToBeWritten * 4);

        // Write postings to block
        while (true){//while (plIterator.hasNext()) {
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

                docsMemOffset += (int) (nPostingsToBeWritten * 4L);
                freqsMemOffset += (int) (nPostingsToBeWritten * 4L);
                break;
            }
        }
    }


    public static Merger2 with(Config config, int numIndexes) {
        instance = new Merger2(config, numIndexes);
        return instance;
    }

    //TESTING
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

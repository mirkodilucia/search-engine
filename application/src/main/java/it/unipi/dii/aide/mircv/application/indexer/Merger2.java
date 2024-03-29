package it.unipi.dii.aide.mircv.application.indexer;

import it.unipi.dii.aide.mircv.application.compression.UnaryCompressor;
import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.*;
import it.unipi.dii.aide.mircv.application.compression.VariableByteCompressor;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.nio.MappedByteBuffer;

public class Merger2 {

    private static int numIndexes;
    private static VocabularyEntry[] nextTerms;
    private final long[] vocabularyEntryMemoryOffset;
    private static Merger2 instance = null;

    private final FileChannel[] docIdChannels;
    private final FileChannel[] frequencyChannels;

    private int docsMemOffset;
    private int freqsMemOffset;


    private final Config config;
    private MergerLoader loader;

    private Merger2(Config config, int numIndexes) {
        this.config = config;

        nextTerms = new VocabularyEntry[numIndexes];
        vocabularyEntryMemoryOffset = new long[numIndexes];

        docIdChannels = new FileChannel[numIndexes];
        frequencyChannels = new FileChannel[numIndexes];

        this.loader = new MergerLoader(config, docIdChannels, frequencyChannels);

        freqsMemOffset = 0;
        docsMemOffset = 0;

        try {
            for (int i = 0; i < numIndexes; i++) {
                nextTerms[i] = new VocabularyEntry();
                vocabularyEntryMemoryOffset[i] = 0;

                long ret = nextTerms[i].readFromDisk(vocabularyEntryMemoryOffset[i], config.getPartialVocabularyPath(i) + "_" + i);
                if (ret == -1 || ret == 0) {
                    nextTerms[i] = null;
                }

                docIdChannels[i] = FileChannelUtils.openFileChannel(config.getPartialIndexDocsPath(i),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                frequencyChannels[i] = FileChannelUtils.openFileChannel(config.getPartialIndexFreqsPath(i),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
            }
        } catch (Exception e) {
            cleanup();
            e.printStackTrace();
        }
    }

    private void cleanup() {
        try {
            for (int i = 0; i < numIndexes; i++) {
                if (docIdChannels[i] != null)
                    docIdChannels[i].close();
                if (frequencyChannels[i] != null)
                    frequencyChannels[i].close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mergeIndexes(int numIndexes,
                             boolean compressionMode,
                             boolean debugMode) {

        long vocabularySize = 0;
        long vocabularyMemoryOffset = 0;

        try (
                FileChannel vocabularyChannel =
                        FileChannelUtils.openFileChannel(config.getPathToVocabulary(),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE);
                FileChannel documentIdChannel =
                        FileChannelUtils.openFileChannel(config.getPathToInvertedIndexDocs(),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE);
                FileChannel frequencyChan =
                        FileChannelUtils.openFileChannel(config.getPathToInvertedIndexDocs(),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE);
                FileChannel descriptorChan =
                        FileChannelUtils.openFileChannel(config.getPathToInvertedIndexDocs(),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE);
        ) {

            while(true) {
                String termToProcess = MergerWorker.with(config).getMinimumTerm(nextTerms, numIndexes);

                if (termToProcess == null)
                    break;

                VocabularyEntry vocabularyEntry = new VocabularyEntry(termToProcess);
                PostingList mergedPostingList = MergerWorker.with(config).processTerm(loader, nextTerms, vocabularyEntry, termToProcess);

                if(mergedPostingList == null){
                    throw new Exception("ERROR: the merged posting list for the term " + termToProcess + " is null");
                }

                vocabularyEntry.computeBlocksInformation();
                int maxNumPostings = vocabularyEntry.getMaxNumberOfPostingsInBlock();

                Iterator<Posting> plIterator = mergedPostingList.getPostings().iterator();
                int numBlocks = vocabularyEntry.getNumBlocks();

                for(int i=0; i< numBlocks; i++) {
                    BlockDescriptor blockDescriptor = new BlockDescriptor();
                    blockDescriptor.setDocidOffset(docsMemOffset);
                    blockDescriptor.setFreqOffset(freqsMemOffset);

                    int nPostingsToBeWritten = getnPostingsToBeWritten(i, maxNumPostings, mergedPostingList);

                    if (compressionMode) {
                        processCompressedPostingList(plIterator, blockDescriptor, nPostingsToBeWritten, maxNumPostings, documentIdChannel, frequencyChan, descriptorChan);
                    }else {
                        processUncompressedPostingList(plIterator, blockDescriptor, nPostingsToBeWritten, documentIdChannel, frequencyChan, descriptorChan);
                    }
                }

                vocabularyMemoryOffset = vocabularyEntry.writeEntry(vocabularyMemoryOffset, vocabularyChannel);
                vocabularySize++;

                if(debugMode){
                    mergedPostingList.debugSaveToDisk("debugDOCIDS.txt", "debugFREQS.txt", maxNumPostings);
                    vocabularyEntry.debugSaveToDisk("debugVOCABULARY.txt");
                }
            }

            cleanup();
            DocumentCollectionSize.updateVocabularySize(vocabularySize, config.getCollectionStatisticsPath());
        } catch (Exception ex) {

        }

    }

    private static int getnPostingsToBeWritten(int i, int maxNumPostings, PostingList mergedPostingList) {
        return (Math.min((mergedPostingList.getPostings().size() - i * maxNumPostings), maxNumPostings));
    }

    private void processCompressedPostingList(
            Iterator<Posting> plIterator,
            BlockDescriptor blockDescriptor,
            int nPostingsToBeWritten,
            int maxNumPostings,
            FileChannel docidChan,
            FileChannel frequencyChan,
            FileChannel descriptorChan
    ) throws IOException {

        int postingsInBlock = 0;
        int[] docids = new int[nPostingsToBeWritten];
        int[] freqs = new int[nPostingsToBeWritten];

        // Initialize docids and freqs arrays
        while (plIterator.hasNext()) {
            Posting currPosting = plIterator.next();
            docids[postingsInBlock] = currPosting.getDocId();
            freqs[postingsInBlock] = currPosting.getFrequency();

            postingsInBlock++;

            if (postingsInBlock == nPostingsToBeWritten) {
                // TODO: controlla che la encode vada bene
                byte[] compressedDocs = VariableByteCompressor.encode(docids);
                byte[] compressedFreqs = UnaryCompressor.integerArrayCompression(freqs);

                // Write compressed posting lists to disk
                this.loader.writeCompressedPostingListsToDisk(currPosting, docidChan, frequencyChan, descriptorChan,
                        compressedDocs, compressedFreqs, blockDescriptor, docsMemOffset, freqsMemOffset, maxNumPostings);
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

    public static void with(Config config, int numIndexes) {
        instance = new Merger2(config, numIndexes);
    }
}

package it.unipi.dii.aide.mircv.indexer.merger;

import it.unipi.dii.aide.mircv.compress.UnaryCompressor;
import it.unipi.dii.aide.mircv.compress.VariableByteCompressor;
import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.indexer.model.BlockDescriptor;
import it.unipi.dii.aide.mircv.indexer.model.Posting;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;

public class Merger {

    private final Config config;

    private long vocabularyMemoryOffset;
    private long documentsMemoryOffset;
    private long frequenciesMemoryOffset;
    private int vocabularySize;

    private Merger(Config config) {
        this.config = config;
    }

    public static Merger with(Config config) {
        VocabularyEntry.setupPath(config);
        PostingList.setupPath(config);
        MergerFileChannel.setupPath(config);
        DocumentIndexState.with(config);
        return new Merger(config);
    }

    /**
     * print the performance statistics
     */
    public void printPerformanceStatistics() {
        System.out.println("Inverted index's memory occupancy:");
        System.out.println("\t> docids: "+ documentsMemoryOffset + "bytes");
        System.out.println("\t> freqs: "+ frequenciesMemoryOffset + "bytes");
    }

    /**
     * Merge the indexes into a single one and save it to disk using the configuration object provided
     * @param indexes the number of indexes to merge
     * @return true if the indexes have been merged, false otherwise
     */
    public boolean mergeIndexes(int indexes) {
        try {
            MergerFileChannel margerFileChannels = MergerFileChannel.open(config);

            MergerWorker worker = MergerWorker.with(config, indexes);
            while (true) {
                String termToProcess = worker.getMinimumTerm();
                if (termToProcess == null) {
                    break;
                }

                VocabularyEntry vocabularyEntry = new VocabularyEntry(termToProcess);
                PostingList mergedPostingList = worker.processTerm(vocabularyEntry, termToProcess, documentsMemoryOffset, frequenciesMemoryOffset);

                if (mergedPostingList == null) {
                    throw new IOException("Error while processing term " + termToProcess);
                }

                vocabularyEntry.computeBlockInformation();
                int maxNumPostings = vocabularyEntry.getMaxNumberOfPostingInBlock();

                iteratePostingList(margerFileChannels, vocabularyEntry, mergedPostingList, maxNumPostings);

                vocabularyMemoryOffset = vocabularyEntry.writeEntry(vocabularyMemoryOffset, margerFileChannels.vocabularyChannel);

                vocabularySize++;

                if (config.debug) {
                    System.out.println("Vocabulary entry written to disk: " + vocabularyEntry);

                    mergedPostingList.debugSaveToDisk("debugDOCIDS.txt", "debugFREQS.txt", maxNumPostings);
                    vocabularyEntry.debugSaveToDisk("debugVOCABULARY.txt");
                }
            }


            DocumentIndexState.updateVocabularySize(vocabularySize);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Iterate the posting list and write the blocks to disk using the file channels provided
     * @param mergerFileChannels the file channels to write the blocks
     * @param entry the vocabulary entry to process
     * @param mergedPostingList the posting list to process
     * @param maxNumPostings the maximum number of postings in a block
     * @return the compression result
     */
    private MergerFileChannel.CompressionResult iteratePostingList(MergerFileChannel mergerFileChannels, VocabularyEntry entry, PostingList mergedPostingList, int maxNumPostings) {
        Iterator<Posting> postingListIterator = mergedPostingList.getPostings().iterator();
        int numBlocks = entry.getHowManyBlockToWrite();

        MergerFileChannel.CompressionResult result = new MergerFileChannel.CompressionResult(documentsMemoryOffset, frequenciesMemoryOffset);

        for (int i=0; i<numBlocks; i++) {
            BlockDescriptor blockDescriptor = new BlockDescriptor(
                    documentsMemoryOffset,
                    frequenciesMemoryOffset
            );

            int nPostingsToBeWritten = mergedPostingList.getPostingsToBeWritten(i, maxNumPostings);
            MergerPostingIteration mergerPostingIteration = new MergerPostingIteration(postingListIterator, blockDescriptor, nPostingsToBeWritten, maxNumPostings);

            MergerFileChannel.CompressionResult intermediateResult;

            if (config.getBlockDescriptorConfig().isCompressionEnabled()) {
                intermediateResult = processCompressedPostingList(mergerPostingIteration, mergerFileChannels);
                result.updateCompressionResult(intermediateResult);
            }else {
                intermediateResult = processUncompressedPostingList(mergerPostingIteration, mergerFileChannels);
                result.updateCompressionOffset(intermediateResult);
            }
        }

        return result;
    }

    /**
     * Process the uncompressed posting list and write the blocks to disk using the file channels provided
     * @param mergerPostingIteration the posting iteration to process
     * @param mergerFileChannels the file channels to write the blocks
     * @return the compression result
     */
    private MergerFileChannel.CompressionResult processUncompressedPostingList(MergerPostingIteration mergerPostingIteration, MergerFileChannel mergerFileChannels) {
        int postingInBlock = 0;
        int nPostingsToBeWritten = mergerPostingIteration.nPostingsToBeWritten * 4;

        MergerFileChannel.CompressionResult result = new MergerFileChannel.CompressionResult();

        try {
            MappedByteBuffer docsBuffer = mergerFileChannels.documentIdChannel.map(FileChannel.MapMode.READ_WRITE, documentsMemoryOffset, nPostingsToBeWritten);
            MappedByteBuffer freqsBuffer = mergerFileChannels.frequencyChannel.map(FileChannel.MapMode.READ_WRITE, frequenciesMemoryOffset, nPostingsToBeWritten);

            // Posting list must not be compressed
            // Set docs and freqs num bytes as (number of postings) * 4
            mergerPostingIteration.blockDescriptor.setDocumentIdSize(nPostingsToBeWritten);
            mergerPostingIteration.blockDescriptor.setFrequenciesSize(nPostingsToBeWritten);

            while (mergerPostingIteration.postingListIterator.hasNext()) {
                Posting currentPosting = mergerPostingIteration.postingListIterator.next();

                docsBuffer.putInt(currentPosting.getDocumentId());
                freqsBuffer.putInt(currentPosting.getFrequency());

                postingInBlock++;

                if (postingInBlock == mergerPostingIteration.nPostingsToBeWritten) {
                    mergerPostingIteration.blockDescriptor.setMaxDocumentsId(currentPosting.getDocumentId());
                    mergerPostingIteration.blockDescriptor.setNumPostings(postingInBlock);

                    mergerPostingIteration.writeBlock(mergerFileChannels.descriptorChannel);
                    result.updateCompressionOffset(
                            mergerPostingIteration.nPostingsToBeWritten * 4L,
                            mergerPostingIteration.nPostingsToBeWritten * 4L
                            );

                    documentsMemoryOffset += mergerPostingIteration.nPostingsToBeWritten * 4L;
                    frequenciesMemoryOffset += mergerPostingIteration.nPostingsToBeWritten * 4L;
                    break;
                }
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Process the compressed posting list and write the blocks to disk using the file channels provided
     * @param mergerPostingIteration the posting iteration to process
     * @param mergerFileChannels the file channels to write the blocks
     * @return the compression result
     */
    private MergerFileChannel.CompressionResult processCompressedPostingList(MergerPostingIteration mergerPostingIteration, MergerFileChannel mergerFileChannels) {
        int postingInBlock = 0;
        int nPostingsToBeWritten = mergerPostingIteration.nPostingsToBeWritten;

        MergerFileChannel.CompressionResult result = new MergerFileChannel.CompressionResult();

        try {
            int[] docIds = new int[nPostingsToBeWritten];
            int[] freqs = new int[nPostingsToBeWritten];

            while (mergerPostingIteration.postingListIterator.hasNext()) {
                Posting currentPosting = mergerPostingIteration.postingListIterator.next();

                docIds[postingInBlock] = currentPosting.getDocumentId();
                freqs[postingInBlock] = currentPosting.getFrequency();

                postingInBlock++;

                if (postingInBlock == mergerPostingIteration.nPostingsToBeWritten) {
                    byte[] compressedDocs = VariableByteCompressor.integerArrayCompression(docIds);
                    byte[] compressedFreqs = UnaryCompressor.integerArrayCompression(freqs);

                    MappedByteBuffer docsBuffer = mergerFileChannels.documentIdChannel.map(FileChannel.MapMode.READ_WRITE, documentsMemoryOffset, compressedDocs.length);
                    MappedByteBuffer freqsBuffer = mergerFileChannels.frequencyChannel.map(FileChannel.MapMode.READ_WRITE, frequenciesMemoryOffset, compressedFreqs.length);

                    docsBuffer.put(compressedDocs);
                    freqsBuffer.put(compressedFreqs);

                    mergerPostingIteration.blockDescriptor.setDocumentIdSize(compressedDocs.length);
                    mergerPostingIteration.blockDescriptor.setFrequenciesSize(compressedFreqs.length);

                    mergerPostingIteration.blockDescriptor.setMaxDocumentsId(currentPosting.getDocumentId());
                    mergerPostingIteration.blockDescriptor.setNumPostings(postingInBlock);

                    mergerPostingIteration.writeBlock(mergerFileChannels.descriptorChannel);
                    result.updateCompressionOffset(
                            compressedDocs.length,
                            compressedFreqs.length
                    );

                    documentsMemoryOffset += compressedDocs.length;
                    frequenciesMemoryOffset += compressedFreqs.length;

                    break;
                }
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unset the configuration object
     */
    public void unset() {
        MergerWorker.unset(config);
    }
}
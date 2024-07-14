package it.unipi.dii.aide.mircv.indexer.merger;

import it.unipi.dii.aide.mircv.config.Config;
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

                MergerFileChannel.CompressionResult compressionResult = iteratePostingList(margerFileChannels, vocabularyEntry, mergedPostingList, maxNumPostings);

                vocabularyMemoryOffset = vocabularyEntry.writeEntry(vocabularyMemoryOffset, margerFileChannels.vocabularyChannel);

                documentsMemoryOffset += compressionResult.docsMemOffset;
                frequenciesMemoryOffset += compressionResult.freqsMemOffset;

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

            if (config.compression) {
                intermediateResult = processCompressedPostingList(mergerPostingIteration, mergerFileChannels);
                result.updateCompressionResult(intermediateResult);
            }else {
                intermediateResult = processUncompressedPostingList(mergerPostingIteration, mergerFileChannels);
                result.updateCompressionOffset(intermediateResult);
            }
        }

        return result;
    }

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
                    break;
                }
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private MergerFileChannel.CompressionResult processCompressedPostingList(MergerPostingIteration mergerPostingIteration, MergerFileChannel mergerFileChannels) {
        int postingInBlock = 0;
        int nPostingsToBeWritten = mergerPostingIteration.nPostingsToBeWritten * 4;

        MergerFileChannel.CompressionResult result = new MergerFileChannel.CompressionResult();

        try {
            MappedByteBuffer docsBuffer = mergerFileChannels.documentIdChannel.map(FileChannel.MapMode.READ_WRITE, documentsMemoryOffset, nPostingsToBeWritten);
            MappedByteBuffer freqsBuffer = mergerFileChannels.frequencyChannel.map(FileChannel.MapMode.READ_WRITE, frequenciesMemoryOffset, nPostingsToBeWritten);

            // Posting list must not be compressed
            // Set docs and freqs num bytes as (number of postings)*4
            mergerPostingIteration.blockDescriptor.setDocumentIdSize(nPostingsToBeWritten);
            mergerPostingIteration.blockDescriptor.setFrequenciesSize(nPostingsToBeWritten);

            while (mergerPostingIteration.postingListIterator.hasNext()) {
                Posting currentPosting = mergerPostingIteration.postingListIterator.next();

                postingInBlock++;

                if (postingInBlock == mergerPostingIteration.nPostingsToBeWritten) {
                    mergerPostingIteration.blockDescriptor.setMaxDocumentsId(currentPosting.getDocumentId());
                    mergerPostingIteration.blockDescriptor.setNumPostings(postingInBlock);

                    MergerFileChannel.CompressionResult intermediateResult = mergerPostingIteration.writeBlock(mergerFileChannels.descriptorChannel);
                    result.updateCompressionResult(intermediateResult);
                }
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void unset() {
        MergerWorker.unset(config);


    }
}
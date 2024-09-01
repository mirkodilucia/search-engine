package it.unipi.dii.aide.mircv.indexer.merger;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class MergerFileChannel {
    private static String VOCABULARY_FILE_PATH = "data/vocabulary/vocabulary_0.dat";
    private static String INVERTED_DOCUMENT_INDEX_FILE_PATH = "data/inverted/inverted_index.dat";
    private static String INVERTED_DOCUMENT_FREQUENCY_FILE_PATH = "data/inverted/inverted_index_frequency.dat";
    private static String BLOCK_DESCRIPTOR_FILE_PATH = "data/inverted/block_descriptor.dat";

    FileChannel vocabularyChannel;
    FileChannel documentIdChannel;
    FileChannel frequencyChannel;
    FileChannel descriptorChannel;

    public static void setupPath(Config config) {
        VOCABULARY_FILE_PATH = config.getVocabularyPath();
        INVERTED_DOCUMENT_INDEX_FILE_PATH = config.invertedIndexConfig.getInvertedIndexDocs();
        INVERTED_DOCUMENT_FREQUENCY_FILE_PATH = config.invertedIndexConfig.getInvertedIndexFreqsFile();
        BLOCK_DESCRIPTOR_FILE_PATH = config.getBlockDescriptorsPath();
    }

    /**
     * Constructor of the MergerFileChannel class that takes the file channels for the vocabulary, the inverted index and the block descriptor
     * @param vocabularyChannel the file channel for the vocabulary
     * @param documentIdChannel the file channel for the inverted index
     * @param frequencyChannel the file channel for the inverted index frequency
     * @param descriptorChannel the file channel for the block descriptor
     */
    private MergerFileChannel(
            FileChannel vocabularyChannel,
            FileChannel documentIdChannel, FileChannel frequencyChannel, FileChannel descriptorChannel) {
        this.vocabularyChannel = vocabularyChannel;
        this.documentIdChannel = documentIdChannel;
        this.frequencyChannel = frequencyChannel;
        this.descriptorChannel = descriptorChannel;
    }

    /**
     * Open the file channels for the vocabulary, the inverted index and the block descriptor
     * @param config the configuration object
     * @return the MergerFileChannel object
     * @throws IOException if an I/O error occurs
     */
    public static MergerFileChannel open(Config config) throws IOException {
        return new MergerFileChannel(FileChannelHandler.open(VOCABULARY_FILE_PATH, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE),
                FileChannelHandler.open(INVERTED_DOCUMENT_INDEX_FILE_PATH, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE),
                FileChannelHandler.open(INVERTED_DOCUMENT_FREQUENCY_FILE_PATH, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE),
                FileChannelHandler.open(BLOCK_DESCRIPTOR_FILE_PATH, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE)
        );
    }

    public static class CompressionResult {
        public byte[] compressedDocs;
        public byte[] compressedFreqs;
        public long docsMemOffset;
        public long freqsMemOffset;

        public CompressionResult() {
            this.compressedDocs = new byte[0];
            this.compressedFreqs = new byte[0];
        }

        public CompressionResult(long documentsMemoryOffset, long frequenciesMemoryOffset) {
            this.docsMemOffset = documentsMemoryOffset;
            this.freqsMemOffset = frequenciesMemoryOffset;
        }

        public CompressionResult updateCompressionOffset(long docsMemOffset, long freqsMemOffset) {
            this.docsMemOffset += docsMemOffset;
            this.freqsMemOffset += freqsMemOffset;
            return this;
        }

        /**
         * Update the compression result with the intermediate result
         * @param result the intermediate result
         */
        public void updateCompressionResult(CompressionResult result) {
            if (result.compressedDocs == null || result.compressedDocs.length == 0)
                this.compressedDocs = result.compressedDocs;
            else {
                // Append compressedDocs
                this.compressedDocs = new byte[this.compressedDocs.length + result.compressedDocs.length];
                for (int i = 0; i < result.compressedDocs.length; i++) {
                    this.compressedDocs[i] = result.compressedDocs[i];
                }
            }
            docsMemOffset += result.docsMemOffset;

            if (result.compressedFreqs == null || result.compressedFreqs.length == 0)
                this.compressedFreqs = result.compressedFreqs;
            else {
                // Append compressedFreqs
                this.compressedFreqs = new byte[this.compressedFreqs.length + result.compressedFreqs.length];
                for (int i = 0; i < result.compressedFreqs.length; i++) {
                    this.compressedFreqs[i] = result.compressedFreqs[i];
                }
            }
            freqsMemOffset += result.freqsMemOffset;
        }

        public void updateCompressionOffset(CompressionResult intermediateResult) {
            this.docsMemOffset = intermediateResult.docsMemOffset;
            this.freqsMemOffset = intermediateResult.freqsMemOffset;
        }
    }
}
package it.unipi.dii.aide.mircv.indexer.merger;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.indexer.model.BlockDescriptor;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.BaseVocabularyEntry;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;
import it.unipi.dii.aide.mircv.compress.UnaryCompressor;
import it.unipi.dii.aide.mircv.compress.VariableByteCompressor;

import java.io.IOException;
import java.nio.MappedByteBuffer;
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

    private MergerFileChannel(
            FileChannel vocabularyChannel,
            FileChannel documentIdChannel, FileChannel frequencyChannel, FileChannel descriptorChannel) {
        this.vocabularyChannel = vocabularyChannel;
        this.documentIdChannel = documentIdChannel;
        this.frequencyChannel = frequencyChannel;
        this.descriptorChannel = descriptorChannel;
    }

    public static MergerFileChannel open(Config config) throws IOException {
        return new MergerFileChannel(FileChannelHandler.open(VOCABULARY_FILE_PATH, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE),
                FileChannelHandler.open(INVERTED_DOCUMENT_INDEX_FILE_PATH, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE),
                FileChannelHandler.open(INVERTED_DOCUMENT_FREQUENCY_FILE_PATH, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE),
                FileChannelHandler.open(BLOCK_DESCRIPTOR_FILE_PATH, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE)
        );
    }

    public CompressionResult writeCompressedBlock(BlockDescriptor blockDescriptor,
                                     MergerFileChannel mergerFileChannel,
                                     int documentsMemOffset,
                                     int frequenciesMemOffset,
                                     int[] documentsId, int[] frequencies
    ) {
        try {
            byte[] compressedDocs = VariableByteCompressor.integerArrayCompression(documentsId);
            byte[] compressedFreqs = UnaryCompressor.integerArrayCompression(frequencies);

            // Instantiation of MappedByteBuffer for integer list of docids and for integer list of freqs
            MappedByteBuffer docsBuffer = mergerFileChannel.documentIdChannel.map(FileChannel.MapMode.READ_WRITE, documentsMemOffset, compressedDocs.length);
            MappedByteBuffer freqsBuffer = mergerFileChannel.frequencyChannel.map(FileChannel.MapMode.READ_WRITE, frequenciesMemOffset, compressedFreqs.length);

            // Write compressed posting lists to disk
            docsBuffer.put(compressedDocs);
            freqsBuffer.put(compressedFreqs);

            BaseVocabularyEntry.VocabularyMemoryInfo memoryInfo = new BaseVocabularyEntry.VocabularyMemoryInfo(
                    docsBuffer.position(),
                    freqsBuffer.position(),
                    compressedDocs.length,
                    compressedFreqs.length
            );

            // Write the block descriptor on disk
            CompressionResult result = blockDescriptor.writeBlock(mergerFileChannel.descriptorChannel);
            if (result == null) {
                System.err.println("Error while writing block descriptor on disk.");
                throw new IOException();
            }

            return new CompressionResult(compressedDocs, compressedFreqs, documentsMemOffset, frequenciesMemOffset);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static class CompressionResult {
        public byte[] compressedDocs;
        public byte[] compressedFreqs;
        public long docsMemOffset;
        public long freqsMemOffset;

        public CompressionResult(byte[] compressedDocs, byte[] compressedFreqs, int docsMemOffset, int freqsMemOffset) {
            this.compressedDocs = compressedDocs;
            this.compressedFreqs = compressedFreqs;
            this.docsMemOffset = docsMemOffset;
            this.freqsMemOffset = freqsMemOffset;
        }

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
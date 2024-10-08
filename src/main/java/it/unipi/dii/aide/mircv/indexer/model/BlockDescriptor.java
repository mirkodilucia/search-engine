package it.unipi.dii.aide.mircv.indexer.model;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.indexer.merger.MergerFileChannel;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.nio.MappedByteBuffer;

public class BlockDescriptor extends BaseBlockDescriptor {

    private static String INVERTED_INDEX_DOCS = "data/inverted_index_docs";
    private static String INVERTED_INDEX_FREQS = "data/inverted_index_freqs";

    public static FileChannel docsFChan;
    private static FileChannel freqsFChan;

    public BlockDescriptor() {
        super();
    }

    public BlockDescriptor(long documentsMemoryOffset, long frequenciesMemoryOffset) {
        super(documentsMemoryOffset, frequenciesMemoryOffset);
    }

    /**
     * Initialize the block descriptor with the configuration object
     * @param config the configuration object
     */
    public static void init(Config config) {
        setupPath(config);

        if (docsFChan != null && docsFChan.isOpen() &&
            freqsFChan != null && freqsFChan.isOpen()) {
            return;
        }

        try {
            docsFChan = (FileChannel) Files.newByteChannel(Paths.get(INVERTED_INDEX_DOCS),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.READ,
                    StandardOpenOption.CREATE
            );
            freqsFChan = (FileChannel) Files.newByteChannel(Paths.get(INVERTED_INDEX_FREQS),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.READ,
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup the path to save the inverted index
     * @param config the configuration object
     */
    private static void setupPath(Config config) {
        COMPRESSION_ENABLED = config.getCompressionEnabled();
        INVERTED_INDEX_DOCS = config.getInvertedIndexDocs();
        INVERTED_INDEX_FREQS = config.getInvertedIndexFreqsFile();
    }

    /**
     * Write the block on the disk using the file channel
     * @param descriptorChannel the file channel to write the block
     * @return true if the block has been written, false otherwise
     */
    public MergerFileChannel.CompressionResult writeBlock(FileChannel descriptorChannel) {
        boolean result = writeBlockOnDisk(descriptorChannel);
        if (result) {
            return new MergerFileChannel.CompressionResult();
        }

        return null;
    }

    /**
     * Read the block postings from the disk
     * @return the block postings
     */
    public ArrayList<Posting> getBlockPostings() {
        try{

            MappedByteBuffer documentsBuffer = readDocumentsBuffer(docsFChan);
            MappedByteBuffer frequencyBuffer = readFrequenciesBuffer(freqsFChan);

            if(documentsBuffer == null || frequencyBuffer == null){
                return null;
            }

            if (COMPRESSION_ENABLED) {
                return getCompressedPosting(documentsBuffer, frequencyBuffer);
            }

            return getBlockPosting(documentsBuffer, frequencyBuffer);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void reset() {
        BaseBlockDescriptor.reset();
    }

}

package it.unipi.dii.aide.mircv.indexer.model;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.indexer.merger.MergerFileChannel;

import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.nio.MappedByteBuffer;

public class BlockDescriptor extends BaseBlockDescriptor {

    private static String INVERTED_INDEX_DOCS = "data/inverted_index_docs";
    private static String INVERTED_INDEX_FREQS = "data/inverted_index_freqs";


    public BlockDescriptor() {
        super();
    }

    public BlockDescriptor(long documentsMemoryOffset, long frequenciesMemoryOffset) {
        super(documentsMemoryOffset, frequenciesMemoryOffset);
    }

    public static void init(Config config) {
        setupPath(config);
    }

    private static void setupPath(Config config) {
        COMPRESSION_ENABLED = config.getCompressionEnabled();
        INVERTED_INDEX_DOCS = config.getInvertedIndexDocs();
        INVERTED_INDEX_FREQS = config.getInvertedIndexFreqsFile();
    }

    public MergerFileChannel.CompressionResult writeBlock(FileChannel descriptorChannel) {
        boolean result = writeBlockOnDisk(descriptorChannel);
        if (result) {
            return new MergerFileChannel.CompressionResult();
        }

        return null;
    }

    public ArrayList<Posting> getBlockPostings() {
        try(
                FileChannel docsFChan = (FileChannel) Files.newByteChannel(Paths.get(INVERTED_INDEX_DOCS),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE
                );
                FileChannel freqsFChan = (FileChannel) Files.newByteChannel(Paths.get(INVERTED_INDEX_FREQS),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
        ){

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

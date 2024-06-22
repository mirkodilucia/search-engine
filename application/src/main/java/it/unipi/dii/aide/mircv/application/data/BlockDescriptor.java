package it.unipi.dii.aide.mircv.application.data;


import it.unipi.dii.aide.mircv.application.compression.UnaryCompressor;
import it.unipi.dii.aide.mircv.application.compression.VariableByteCompressor;
import it.unipi.dii.aide.mircv.application.config.Config;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 * Represents a block of postings in a posting list, used for implementing skipping.
 */
public class BlockDescriptor {

    // Fields representing the characteristics of the block
    private long docidOffset;
    private int docidSize;
    private long freqOffset;
    private int freqSize;
    private int maxDocid;
    private int numPostings;

    // Size of a block descriptor entry in bytes
    public static final int BLOCK_DESCRIPTOR_ENTRY_BYTES = 4 * Integer.BYTES + 2 * Long.BYTES;

    // Memory offset reached while writing the block descriptor file
    private static long memoryOffset = 0;

    /**
     * path to the docid file of the inverted index
     */
    private static String INVERTED_INDEX_DOCS = "data/invertedIndexDocs";
    /**
     * path to the frequency file of the inverted index
     */
    private static String INVERTED_INDEX_FREQS = "data/invertedIndexFreqs";


    /**
     * Gets the current memory offset.
     *
     * @return The memory offset.
     */
    public static long getMemoryOffset() {
        return memoryOffset;
    }

    // Setter methods for the block characteristics
    public void setDocidOffset(long docidOffset) {
        this.docidOffset = docidOffset;
    }

    public void setDocidSize(int docidSize) {
        this.docidSize = docidSize;
    }

    public void setFreqOffset(long freqOffset) {
        this.freqOffset = freqOffset;
    }

    public void setFreqSize(int freqSize) {
        this.freqSize = freqSize;
    }

    public int getMaxDocid() {
        return maxDocid;
    }

    public void setMaxDocid(int maxDocid) {
        this.maxDocid = maxDocid;
    }

    public void setNumPostings(int numPostings) {
        this.numPostings = numPostings;
    }

    /**
     * Saves the block descriptor information to the block descriptor file.
     *
     * @param fileChannel The file channel of the block descriptor file.
     * @return True if storing was successful, false otherwise.
     */
    public boolean saveDescriptorOnDisk(FileChannel fileChannel) {
        try {
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, memoryOffset, BLOCK_DESCRIPTOR_ENTRY_BYTES);

            if (buffer != null) {
                // Writing block descriptor information to the buffer
                buffer.putLong(docidOffset);
                buffer.putInt(docidSize);
                buffer.putLong(freqOffset);
                buffer.putInt(freqSize);
                buffer.putInt(maxDocid);
                buffer.putInt(numPostings);

                // Updating memory offset
                memoryOffset += BLOCK_DESCRIPTOR_ENTRY_BYTES;

                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the block's postings from the docid and frequency files.
     *
     * @return ArrayList containing block's postings.
     */
    public ArrayList<Posting> getBlockPostings(Config config) {
        String invertedIndexDocsPath = config.getInvertedIndexConfig().getInvertedIndexDocs();
        String invertedIndexFreqsPath = config.getInvertedIndexConfig().getInvertedIndexFreqsFile();

        try (
                // Opening file channels for docid and frequency files
                FileChannel docsFileChannel = (FileChannel) Files.newByteChannel(Paths.get(INVERTED_INDEX_DOCS),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                FileChannel freqsFileChannel = (FileChannel) Files.newByteChannel(Paths.get(INVERTED_INDEX_FREQS),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
        ) {
            // Mapping byte buffers for docid and frequency files
            MappedByteBuffer docBuffer = docsFileChannel.map(FileChannel.MapMode.READ_ONLY, docidOffset, docidSize);
            MappedByteBuffer freqBuffer = freqsFileChannel.map(FileChannel.MapMode.READ_ONLY, freqOffset, freqSize);

            if (docBuffer == null || freqBuffer == null) {
                return null;
            }

            ArrayList<Posting> block = new ArrayList<>();

            if (config.isCompressionEnabled()) {
                // Compression is enabled
                byte[] compressedDocids = new byte[docidSize];
                byte[] compressedFreqs = new byte[freqSize];

                // Reading compressed docids and frequencies from buffers
                docBuffer.get(compressedDocids, 0, docidSize);
                freqBuffer.get(compressedFreqs, 0, freqSize);

                // Decompressing docids and frequencies
                int[] decompressedDocids = VariableByteCompressor.decode(compressedDocids, numPostings);//, numPostings);
                int[] decompressedFreqs = UnaryCompressor.decode(compressedFreqs, numPostings);//, numPostings);
                //int[] decompressedFreqs = VariableByteCompressor.decode(compressedFreqs, numPostings);//, numPostings);

                // Populating the array list of postings with decompressed information
                for (int i = 0; i < numPostings; i++) {
                    Posting posting = new Posting(decompressedDocids[i], decompressedFreqs[i]);
                    block.add(posting);
                }
            } else {
                // Compression is not enabled
                for (int i = 0; i < numPostings; i++) {
                    // Creating a new posting reading docid and frequency from the buffers
                    Posting posting = new Posting(docBuffer.getInt(), freqBuffer.getInt());
                    block.add(posting);
                }
            }

            return block;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "Block info : " +
                "docidOffset = " + docidOffset +
                ", docidSize = " + docidSize +
                ", freqOffset = " + freqOffset +
                ", freqSize = " + freqSize +
                ", maxDocid = " + maxDocid +
                ", numPostings = " + numPostings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockDescriptor that = (BlockDescriptor) o;
        return docidOffset == that.docidOffset && docidSize == that.docidSize && freqOffset == that.freqOffset && freqSize == that.freqSize && maxDocid == that.maxDocid && numPostings == that.numPostings;
    }

    /**
     * Sets the memory offset for testing purposes.
     *
     * @param memoryOffset The offset to be set.
     */
    public static void setMemoryOffset(long memoryOffset) {
        BlockDescriptor.memoryOffset = memoryOffset;
    }

    /** needed for testing purposes
     * @param invertedIndexDocs: path to be set
     */
    public static void setInvertedIndexDocs(String invertedIndexDocs) {
        INVERTED_INDEX_DOCS = invertedIndexDocs;
    }

    /** needed for testing purposes
     * @param invertedIndexFreqs: path to be set
     */
    public static void setInvertedIndexFreqs(String invertedIndexFreqs) {
        INVERTED_INDEX_FREQS = invertedIndexFreqs;
    }
}

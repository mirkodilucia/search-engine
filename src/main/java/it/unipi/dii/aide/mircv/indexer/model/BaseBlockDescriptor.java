package it.unipi.dii.aide.mircv.indexer.model;

import it.unipi.dii.aide.mircv.compress.UnaryCompressor;
import it.unipi.dii.aide.mircv.compress.VariableByteCompressor;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class BaseBlockDescriptor {

    public static final int BLOCK_DESCRIPTOR_ENTRY_BYTES = 4 * Integer.BYTES + 2 * Long.BYTES;

    protected static boolean COMPRESSION_ENABLED = false;

    private int documentIdSize;

    private long documentIdOffset;

    private int frequencySize;

    private long frequencyOffset;

    private int maxDocumentsId;
    private int numPostings;

    private static long memoryOffset = 0;

    public BaseBlockDescriptor() {}

    public BaseBlockDescriptor(long documentsMemoryOffset, long frequenciesMemoryOffset) {
        this.documentIdOffset = documentsMemoryOffset;
        this.frequencyOffset = frequenciesMemoryOffset;
    }

    protected boolean writeBlockOnDisk(FileChannel fileChannel) {
        try {
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, memoryOffset, BLOCK_DESCRIPTOR_ENTRY_BYTES);

            if (buffer == null)
                return false;

            writeBufferWithBlockDescriptor(buffer);
            memoryOffset += BLOCK_DESCRIPTOR_ENTRY_BYTES;

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    public void setMaxDocumentsId(int maxDocumentsId) {
        this.maxDocumentsId = maxDocumentsId;
    }

    public int getMaxDocumentsId() {
        return maxDocumentsId;
    }

    public void setNumPostings(int numPostings) {
        this.numPostings = numPostings;
    }

    public void setDocumentIdSize(int size) {
        this.documentIdSize = size;
    }

    public void setFrequenciesSize(int size) {
        this.frequencySize = size;
    }

    public static long getMemoryOffset() {
        return memoryOffset;
    }

    public void mapBlockDescriptor(MappedByteBuffer buffer) {
        documentIdOffset = buffer.getLong();
        documentIdSize = buffer.getInt();

        frequencyOffset = buffer.getLong();
        frequencySize = buffer.getInt();

        maxDocumentsId = buffer.getInt();
        numPostings = buffer.getInt();
    }

    public void writeBufferWithBlockDescriptor(MappedByteBuffer buffer) {
        buffer.putLong(documentIdOffset);
        buffer.putInt(documentIdSize);

        buffer.putLong(frequencyOffset);
        buffer.putInt(frequencySize);

        buffer.putInt(maxDocumentsId);
        buffer.putInt(numPostings);
    }

    protected MappedByteBuffer readDocumentsBuffer(FileChannel docsFChan) throws IOException {
        return docsFChan.map(
                FileChannel.MapMode.READ_ONLY,
                documentIdOffset,
                documentIdSize
        );
    }

    protected MappedByteBuffer readFrequenciesBuffer(FileChannel freqFChan) throws IOException {
        return freqFChan.map(
                FileChannel.MapMode.READ_ONLY,
                frequencyOffset,
                frequencySize
        );
    }

    protected ArrayList<Posting> getCompressedPosting(
            MappedByteBuffer documentBuffer,
            MappedByteBuffer frequencyBuffer
    ) {
        ArrayList<Posting> postings = new ArrayList<>();

        byte[] compressedDocumentsIds = new byte[documentIdSize];
        byte[] compressedFrequencies = new byte[frequencySize];

        // read bytes from file
        documentBuffer.get(compressedDocumentsIds, 0, documentIdSize);
        frequencyBuffer.get(compressedFrequencies, 0, frequencySize);

        // perform decompression of docids and frequencies
        int[] decompressedDocumentIds = VariableByteCompressor.decode(compressedDocumentsIds, numPostings);
        int[] decompressedFrequencies = UnaryCompressor.decode(compressedFrequencies, numPostings);

        // populate the array list of postings with the decompressed information about block postings
        for(int i=0; i<numPostings; i++){
            Posting posting = new Posting(decompressedDocumentIds[i], decompressedFrequencies[i]);
            postings.add(posting);
        }

        return postings;
    }

    protected ArrayList<Posting> getBlockPosting(
        MappedByteBuffer documentBuffer,
        MappedByteBuffer frequencyBuffer
    ) {
        ArrayList<Posting> postings = new ArrayList<>();

        for (int i = 0; i < numPostings; i++) {
            Posting posting = new Posting(documentBuffer.getInt(), frequencyBuffer.getInt());
            postings.add(posting);
        }

        return postings;
    }

    public void setDocumentIdOffset(int i) {
        this.documentIdOffset = i;
    }


    public void setFrequenciesOffset(int i) {
        this.frequencyOffset = i;
    }

    public double getDocumentIdOffset() {
        return documentIdOffset;
    }

    public double getDocumentIdSize() {
        return documentIdSize;
    }


    public double getFrequenciesOffset() {
        return frequencyOffset;
    }

    public double getFrequeanciesSize() {
        return frequencySize;
    }

    public double getNumPostings() {
        return numPostings;
    }

    protected static void reset() {
        memoryOffset = 0;
    }

    @Override
    public String toString() {
        return "Block info : " +
                "docidOffset = " + documentIdOffset +
                ", docidSize = " + documentIdSize +
                ", freqOffset = " + frequencyOffset +
                ", freqSize = " + frequencySize +
                ", maxDocid = " + maxDocumentsId +
                ", numPostings = " + numPostings;
    }
}

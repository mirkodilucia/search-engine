package it.unipi.dii.aide.mircv.data;


import it.unipi.dii.aide.mircv.Posting;
import it.unipi.dii.aide.mircv.PostingList;
import it.unipi.dii.aide.mircv.document.data.DocumentCollectionSize;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;

public class VocabularyEntry
{
    private static String BLOCK_DESCRIPTORS_PATH;
    private String term;
    private int documentFrequency;
    private double inverseDocumentFrequency;
    private int maxTermFrequency;
    private int BM25Dl=1; // document length
    private int BM25Tf=0; // term frequency
    private double maxBM25Tf=0; // max term frequency
    private double maxTfIdf=0; // max tf-idf

    private double maxBM25 = 0;

    private long docidOffset=0;
    private int docIdSize=0;

    private long frequencyOffset=0;

    private int frequencySize=0;

    private int numBlocks=1;

    private long blockOffset=0;

    private static final int TERM_SIZE = 64;
    public static final long ENTRY_SIZE = TERM_SIZE + 76;


    public long getDocidOffset() {
        return docidOffset;
    }

    public void setDocidOffset(long docidOffset) {
        this.docidOffset = docidOffset;
    }

    public int getDocIdSize() {
        return docIdSize;
    }

    public void setDocIdSize(int docIdSize) {
        this.docIdSize = docIdSize;
    }

    public long getFrequencyOffset() {
        return frequencyOffset;
    }

    public void setFrequencyOffset(long frequencyOffset) {
        this.frequencyOffset = frequencyOffset;
    }

    public int getFrequencySize() {
        return frequencySize;
    }

    public void setFrequencySize(int frequencySize) {
        this.frequencySize = frequencySize;
    }

    public int getNumBlocks() {
        return numBlocks;
    }

    public void setNumBlocks(int numBlocks) {
        this.numBlocks = numBlocks;
    }

    public long getBlockOffset() {
        return blockOffset;
    }

    public void setBlockOffset(long blockOffset) {
        this.blockOffset = blockOffset;
    }




    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getDocumentFrequency() {
        return documentFrequency;
    }

    public void setDocumentFrequency(int documentFrequency) {
        this.documentFrequency = documentFrequency;
    }

    public double getInverseDocumentFrequency() {
        return inverseDocumentFrequency;
    }

    public void setInverseDocumentFrequency(double inverseDocumentFrequency) {
        this.inverseDocumentFrequency = inverseDocumentFrequency;
    }

    public int getMaxTermFrequency() {
        return maxTermFrequency;
    }

    public void setMaxTermFrequency(int maxTermFrequency) {
        this.maxTermFrequency = maxTermFrequency;
    }

    public int getBM25Dl() {
        return BM25Dl;
    }

    public void setBM25Dl(int BM25Dl) {
        this.BM25Dl = BM25Dl;
    }

    public int getBM25Tf() {
        return BM25Tf;
    }

    public void setBM25Tf(int BM25Tf) {
        this.BM25Tf = BM25Tf;
    }

    public double getMaxBM25Tf() {
        return maxBM25Tf;
    }

    public void setMaxBM25Tf(double maxBM25Tf) {
        this.maxBM25Tf = maxBM25Tf;
    }

    public double getMaxTfIdf() {
        return maxTfIdf;
    }

    public void setMaxTfIdf(double maxTfIdf) {
        this.maxTfIdf = maxTfIdf;
    }

    public VocabularyEntry(String blockDescriptorsPath){}
    public VocabularyEntry(String term, String blockDescriptorsPath){
        BLOCK_DESCRIPTORS_PATH = blockDescriptorsPath;
        this.term = term;
    }

    public void updateValues(PostingList list){
        for (Posting pos : list.getPostings()){
            if(pos.getFrequency() > this.maxTermFrequency){
                this.maxTermFrequency = pos.getFrequency();
            }

            this.documentFrequency ++;
        }
    }

    public void updateBM25Parameters(int length, int tf) {
        double currentRatio = (double) this.BM25Tf / (double) (this.BM25Dl + this.BM25Tf);
        double newRatio = (double) tf / (double) (length + tf);
        if (newRatio > currentRatio) {
            this.BM25Tf = tf;
            this.BM25Dl = length;
        }
    }

    public void calculateInverseDocumentFrequency(){
        this.inverseDocumentFrequency = Math.log10(DocumentCollectionSize.getCollectionSize()/(double)this.documentFrequency);
    }

    public long writeEntry(long postionindex, FileChannel channel){
        //Mappedbuffer
        try {
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, postionindex, ENTRY_SIZE);

            // Buffer not created
            if (buffer == null)
                return -1;

            //allocate char buffer to write term
            CharBuffer charBuffer = CharBuffer.allocate(TERM_SIZE);

            //populate char buffer char by char
            for (int i = 0; i < term.length(); i++)
                charBuffer.put(i, term.charAt(i));

            // Write the term into file
            buffer.put(StandardCharsets.UTF_8.encode(charBuffer));

            // write statistics
            buffer.putInt(documentFrequency);
            buffer.putDouble(inverseDocumentFrequency);

            // write term upper bound information
            buffer.putInt(maxTermFrequency);
            buffer.putInt(BM25Dl);
            buffer.putInt(BM25Tf);
            buffer.putDouble(maxTfIdf);
            buffer.putDouble(maxBM25);

            // write memory information
            buffer.putLong(docidOffset);
            buffer.putLong(frequencyOffset);
            buffer.putInt(docIdSize);
            buffer.putInt(frequencySize);

            // write block information
            buffer.putInt(numBlocks);
            buffer.putLong(blockOffset);

            // return position for which we have to start writing on file
            return postionindex + ENTRY_SIZE;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public long readFromDisk(long memoryOffset, String PATH) {
        // try to open a file channel to the file of the inverted index
        try (FileChannel fChan = (FileChannel) Files.newByteChannel(
                Paths.get(PATH),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE)) {

            // instantiation of MappedByteBuffer for the PID read
            MappedByteBuffer buffer = fChan.map(FileChannel.MapMode.READ_ONLY, memoryOffset, ENTRY_SIZE);

            // Buffer not created
            if (buffer == null)
                return -1;

            // Read from file into the charBuffer, then pass to the string
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);

            String[] encodedTerm = charBuffer.toString().split("\0");
            if (encodedTerm.length == 0)
                return 0;

            this.term = encodedTerm[0];

            // Instantiate the buffer for reading other information
            buffer = fChan.map(FileChannel.MapMode.READ_WRITE, memoryOffset + TERM_SIZE, ENTRY_SIZE - TERM_SIZE);

            // Buffer not created
            if (buffer == null)
                return -1;

            // read statistics
            documentFrequency = buffer.getInt();
            inverseDocumentFrequency = buffer.getDouble();

            // read term upper bound information
            maxTermFrequency = buffer.getInt();
            BM25Dl = buffer.getInt();
            BM25Tf = buffer.getInt();
            maxTfIdf = buffer.getDouble();
            maxBM25 = buffer.getDouble();

            // read memory information
            docidOffset = buffer.getLong();
            frequencyOffset = buffer.getLong();
            docIdSize = buffer.getInt();
            frequencySize = buffer.getInt();

            // write block information
            numBlocks = buffer.getInt();
            blockOffset = buffer.getLong();

            return memoryOffset + ENTRY_SIZE;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * method used to compute the max TFIDF and BM25 used as term upper bounds
     */
    public void computeUpperBounds(){

        // compute term upper bound for TFIDF
        this.maxTfIdf = (1 + Math.log10(this.maxTfIdf)) * this.inverseDocumentFrequency;

        double k1 = 1.5;
        double b = 0.75;
        double avgDocLen = (double) DocumentCollectionSize.getTotalDocumentLen()/DocumentCollectionSize.getCollectionSize();

        this.maxBM25 = (inverseDocumentFrequency * BM25Tf)  / ( BM25Tf + k1 * (1 - b + b * (double)BM25Dl/avgDocLen));

    }

    /**
     * method that computes the number of blocks of postings in which the posting list will be divided.
     * If the number of postings is < 1024 the posting list is stored in a single block.
     */
    public void computeBlocksInformation() {
        this.blockOffset = BlockDescriptor.getMemoryOffset();
        if (documentFrequency >= 1024)
            this.numBlocks = (int) Math.ceil(Math.sqrt(documentFrequency));
    }

    /**
     * computes the max number of postings that we can store in a block
     *
     * @return the max number of postings in a block
     */
    public int getMaxNumberOfPostingsInBlock() {
        return (int) Math.ceil(documentFrequency / (double) numBlocks);
    }

    /**

     * function to write a summarization of the most important data about a vocabulary entry as plain text in the debug file
     * @param path: path of the file where to write
     */
    public void debugSaveToDisk(String path) {
        FileUtils.createDirectory("data/debug");
        FileUtils.createIfNotExists("data/debug/"+path);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("data/debug/"+path, true));
            writer.write(this+"\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * method to read from memory the block descriptors for the term
     * @return the arrayList of the block descriptors
     */
    public ArrayList<BlockDescriptor> readBlocks(){
        try(
                FileChannel fileChannel = (FileChannel) Files.newByteChannel(
                        Paths.get(BLOCK_DESCRIPTORS_PATH),
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE
                )
        ){
            ArrayList<BlockDescriptor> blocks = new ArrayList<>();

            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, blockOffset, (long) numBlocks * BlockDescriptor.BLOCK_DESCRIPTOR_ENTRY_BYTES);

            if(buffer == null)
                return null;
            for(int i = 0; i < numBlocks; i++){
                BlockDescriptor block = new BlockDescriptor();
                block.setDocidOffset(buffer.getLong());
                block.setDocidSize(buffer.getInt());
                block.setFreqOffset(buffer.getLong());
                block.setFreqSize(buffer.getInt());
                block.setMaxDocid(buffer.getInt());
                block.setNumPostings(buffer.getInt());
                blocks.add(block);

            }
            return blocks;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return ", term='" + term + '\'' +
                ", df=" + documentFrequency +
                ", idf=" + inverseDocumentFrequency +
                ", maxTf=" + maxTermFrequency +
                ", BM25Dl=" + BM25Dl +
                ", BM25Tf=" + BM25Tf +
                ", maxTFIDF=" + maxTfIdf +
                ", maxBM25=" + maxBM25 +
                ", docidOffset=" + docidOffset +
                ", frequencyOffset=" + frequencyOffset +
                ", docidSize=" + docIdSize +
                ", frequencySize=" + frequencySize +
                ", numBlocks=" + numBlocks +
                ", blockOffset=" + blockOffset;
    }

    static double truncate(double value) {
        // Using the pow() method
        double newValue = value * Math.pow(10, 4);
        newValue = Math.floor(newValue);
        newValue = newValue / Math.pow(10, 4);
        System.out.println(newValue);
        return newValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VocabularyEntry that = (VocabularyEntry) o;
        return documentFrequency == that.documentFrequency && Double.compare(truncate(that.inverseDocumentFrequency), truncate(inverseDocumentFrequency)) == 0 && maxTermFrequency == that.maxTermFrequency && BM25Dl == that.BM25Dl && BM25Tf == that.BM25Tf && Double.compare(truncate(that.maxTfIdf), truncate(maxTfIdf)) == 0 && Double.compare(truncate(that.maxBM25), truncate(maxBM25)) == 0 && docidOffset == that.docidOffset && frequencyOffset == that.frequencyOffset && docIdSize == that.docIdSize && frequencySize == that.frequencySize && numBlocks == that.numBlocks && blockOffset == that.blockOffset && Objects.equals(term, that.term);
    }

    /**
     * testing purposes
     *
     * @param path the test path for the block descriptors
     */
    public static void setBlockDescriptorsPath(String path) {
        BLOCK_DESCRIPTORS_PATH = path;
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, documentFrequency, inverseDocumentFrequency, maxTermFrequency, BM25Dl, BM25Tf, maxTfIdf, maxBM25, docidOffset, frequencyOffset, docIdSize, frequencySize, numBlocks, blockOffset);
    }



}

package it.unipi.dii.aide.mircv.application.data;

import it.unipi.dii.aide.mircv.application.config.Config;

import java.nio.channels.FileChannel;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DocumentIndexEntry {

    public static final int DOC_ID_SIZE = 64;
    public static final int ENTRY_SIZE = DOC_ID_SIZE + 4 + 4;
    private static String DOCUMENT_INDEX_FILE;
    private Config config;

    private String pId;

    private int documentId;
    private int documentLength;
    private static long memoryOffset = 0;


    public DocumentIndexEntry(Config config){
        //DOCUMENT_INDEX_FILE = config.getDocumentIndexConfig().getDocumentIndexPath();
        DOCUMENT_INDEX_FILE = "../test/data/merger/docIndex";   // TESTING

    }

    public DocumentIndexEntry(Config config, String pId, int documentId, int documentLength) {
        this(config);

        this.config = config;
        this.pId = pId;
        this.documentId = documentId;
        this.documentLength = documentLength;
    }

    public static void reset() {
        memoryOffset = 0;
    }

    public long writeFile(){
        try (FileChannel fc = (FileChannel) Files.newByteChannel(Paths.get(DOCUMENT_INDEX_FILE),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE)) {

            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, memoryOffset, ENTRY_SIZE);

            if (mbb == null)
                return -1;

            CharBuffer cb = CharBuffer.allocate(DOC_ID_SIZE);

            for (int i = 0; i < this.pId.length(); i++) {
                cb.put(i, this.pId.charAt(i));
            }

            mbb.put(StandardCharsets.UTF_8.encode(cb));
            mbb.putInt(this.documentId);
            mbb.putInt(this.documentLength);

            // save the start offset of the structure
            long startOffset = memoryOffset;
            // update memory offset
            memoryOffset = memoryOffset + ENTRY_SIZE;

            return startOffset;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean readFile(long memoryOffset) {
        try (FileChannel fc = (FileChannel) Files.newByteChannel(Paths.get(DOCUMENT_INDEX_FILE),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE)) {

            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, memoryOffset, DOC_ID_SIZE);

            if (mbb == null)
                return false;

            CharBuffer cb = StandardCharsets.UTF_8.decode(mbb);
            if (cb.toString().split("\0").length == 0)
                return true;

            this.pId = cb.toString().split("\0")[0];

            // Instantiate the buffer for reading other information
            mbb = fc.map(FileChannel.MapMode.READ_WRITE, memoryOffset + DOC_ID_SIZE, ENTRY_SIZE - DOC_ID_SIZE);

            // Buffer not created
            if(mbb == null)
                return false;

            this.documentId = mbb.getInt();
            this.documentLength = mbb.getInt();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return "document:" + this.documentId + ":pid:" + this.pId + ":len:" + this.documentLength;
    }

    @Override
    public boolean equals(Object o){

        if(o == this)
            return true;

        if (!(o instanceof DocumentIndexEntry de)) {
            return false;
        }

        return de.getDocumentId() == this.getDocumentId() && de.getPId().equals(this.getPId()) && de.getDocumentLenght() == this.getDocumentLenght();
    }

    public static void resetOffset(){
        memoryOffset = 0;
    }



    public String getPId() {
        return pId;
    }

    public int getDocumentId() {
        return documentId;
    }

    public int getDocumentLenght() {
        return documentLength;
    }

    public void setDocumentLength(int length) {
        this.documentLength = length;
    }

    /**
     * updates the document index path file (only for test purposes)
     */
    public static void setTestPath(){
        DocumentIndexEntry.DOCUMENT_INDEX_FILE = "src/test/data/testDocumentIndex";
        DocumentIndexEntry.memoryOffset = 0;
    }
}

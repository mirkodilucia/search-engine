package it.unipi.dii.aide.mircv.application.data;

import java.nio.channels.FileChannel;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DocumentIndexEntry {

    public static final int DOC_ID_SIZE = 64;
    public static final int ENTRY_SIZE = DOC_ID_SIZE + 4 + 4;
    private static String DOCUMENT_INDEX_PATH;

    private String documentId;
    private int docidId;
    private int documentLength;
    private static long memoryOffset = 0;

    public DocumentIndexEntry(String documentIndexPath){
        DOCUMENT_INDEX_PATH = documentIndexPath;
    }

    public DocumentIndexEntry(String documentId, int docidId, int documentLength) {
        this.documentId = documentId;
        this.docidId = docidId;
        this.documentLength = documentLength;
    }

    public static void reset() {
        memoryOffset = 0;
    }

    public static void setTestPath() {
        DOCUMENT_INDEX_PATH = "../test/data/documentIndex";
    }

    public long writeFile(){
        try (FileChannel fc = (FileChannel.open(Paths.get(DOCUMENT_INDEX_PATH),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE))) {

            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, memoryOffset, ENTRY_SIZE);

            if (mbb == null)
                return -1;

            CharBuffer cb = CharBuffer.allocate(DOC_ID_SIZE);

            for (int i = 0; i < this.documentId.length(); i++) {
                cb.put(i, this.documentId.charAt(i));
            }

            mbb.put(StandardCharsets.UTF_8.encode(cb));
            mbb.putInt(this.docidId);
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
        try (FileChannel fc = (FileChannel.open(Paths.get(DOCUMENT_INDEX_PATH),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE))) {

            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, memoryOffset, DOC_ID_SIZE);

            if (mbb == null)
                return false;

            CharBuffer cb = StandardCharsets.UTF_8.decode(mbb);
            if (cb.toString().split("\0").length == 0)
                return true;

            this.documentId = cb.toString().split("\0")[0];

            // Instantiate the buffer for reading other information
            mbb = fc.map(FileChannel.MapMode.READ_WRITE, memoryOffset + DOC_ID_SIZE, ENTRY_SIZE - DOC_ID_SIZE);

            // Buffer not created
            if(mbb == null)
                return false;

            this.docidId = mbb.getInt();
            this.documentLength = mbb.getInt();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return "document:" + this.documentId + ":pid:" + this.docidId + ":len:" + this.documentLength;
    }

    public String getDocumentId() {
        return documentId;
    }
}

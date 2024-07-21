package it.unipi.dii.aide.mircv.document.table;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class BaseDocumentIndexEntry {

    public static final int DOC_ID_SIZE = 64;
    public static final int ENTRY_SIZE = DOC_ID_SIZE + 4 + 4;

    protected String pId;
    protected int documentId;

    protected int documentLength;
    protected static long memoryOffset = 0;

    protected BaseDocumentIndexEntry(String pId) {
        this.pId = pId;
    }

    protected BaseDocumentIndexEntry(String pId, int documentId, int documentLength) {
        this.pId = pId;
        this.documentId = documentId;
        this.documentLength = documentLength;
    }

    public BaseDocumentIndexEntry() {

    }

    public long writeFile(FileChannel fc) {
        try {
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

    public boolean readFile(long memoryOffset, FileChannel documentIndexFile) {
        try {
            MappedByteBuffer mbb = documentIndexFile.map(FileChannel.MapMode.READ_WRITE, memoryOffset, DOC_ID_SIZE);

            if (mbb == null)
                return false;

            CharBuffer cb = StandardCharsets.UTF_8.decode(mbb);
            if (cb.toString().split("\0").length == 0) {
                this.pId = null;
                return true;
            }
            this.pId = cb.toString().split("\0")[0];

            // Instantiate the buffer for reading other information
            mbb = documentIndexFile.map(FileChannel.MapMode.READ_WRITE, memoryOffset + DOC_ID_SIZE, ENTRY_SIZE - DOC_ID_SIZE);

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

}

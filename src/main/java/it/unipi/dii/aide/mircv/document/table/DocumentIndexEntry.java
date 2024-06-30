package it.unipi.dii.aide.mircv.document.table;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class DocumentIndexEntry extends BaseDocumentIndexEntry {

    private Config config;

    private static final String DOCUMENT_INDEX_FILE = "data/documents/document_index.dat";

    public DocumentIndexEntry(Config config, int documentId) {
        super(DOCUMENT_INDEX_FILE);
        this.config = config;
        this.documentId = documentId;
    }

    public DocumentIndexEntry(Config config, String pId, int documentId, int documentLength) {
        super(pId, documentId, documentLength, DOCUMENT_INDEX_FILE);
        this.config = config;
    }

    public static void reset() {
        memoryOffset = 0;
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

    @Override
    public String toString() {
        return "document:" + this.documentId + ":pid:" + this.pId + ":len:" + this.documentLength;
    }

    public boolean readFile(long memoryOffset, String documentIndexFile) {
        try (
                FileChannel documentIndexFileChannel = FileChannelHandler.open(
                        documentIndexFile,
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE
                )) {
                return this.readFile(memoryOffset, documentIndexFileChannel);
        }catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean writeFile(String documentIndexFile) {
        try (
                FileChannel documentIndexFileChannel = FileChannelHandler.open(
                        documentIndexFile,
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE
                )) {
            return this.readFile(memoryOffset, documentIndexFileChannel);
        }catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String getPId() {
        return pId;
    }
}

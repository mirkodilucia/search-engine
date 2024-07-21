package it.unipi.dii.aide.mircv.document.table;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class DocumentIndexEntry extends BaseDocumentIndexEntry {

    private Config config;

    protected String DOCUMENT_INDEX_FILE = "data/documents/document_index.dat";

    public DocumentIndexEntry(Config config, int documentId) {
        super(config.getDocumentIndexFile());

        DOCUMENT_INDEX_FILE = config.getDocumentIndexFile();

        this.config = config;
        this.documentId = documentId;
    }

    public DocumentIndexEntry(Config config, String pId, int documentId, int documentLength) {
        super(pId, documentId, documentLength);
        this.config = config;
    }

    public DocumentIndexEntry(Config config) {
        super();
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

    public long writeFile(String documentIndexFile) {
        try (
                FileChannel documentIndexFileChannel = FileChannelHandler.open(
                        documentIndexFile,
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE
                )) {
            return this.writeFile(documentIndexFileChannel);
        }catch (IOException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public String getPId() {
        return pId;
    }

    @Override
    public boolean equals(Object o) {

        if(o == this)
            return true;

        if (!(o instanceof DocumentIndexEntry de)) {
            return false;
        }

        return de.getDocumentId() == this.getDocumentId() && de.getPId().equals(this.getPId()) && de.getDocumentLenght() == this.getDocumentLenght();
    }
}

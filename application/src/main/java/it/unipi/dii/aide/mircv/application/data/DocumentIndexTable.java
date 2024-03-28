package it.unipi.dii.aide.mircv.application.data;

import java.util.HashMap;
import it.unipi.dii.aide.mircv.application.config.Config;

public class DocumentIndexTable extends HashMap<String, DocumentIndexEntry> {

    private static DocumentIndexTable INSTANCE = null;
    private static Config configuration;

    private DocumentIndexTable() {
        super();
    }

    public static DocumentIndexTable with(Config config) {
        configuration = config;

        if (INSTANCE == null) {
            INSTANCE = new DocumentIndexTable();
        }
        return INSTANCE;
    }

    public void addEntry(String documentId, int docidId, int docLen) {
        DocumentIndexEntry entry = new DocumentIndexEntry(documentId, docidId, docLen);
    }

    public void load() {
        long numDocuments = DocumentCollectionSize.getCollectionSize();

        for(int i = 0; i < numDocuments; i++) {
            DocumentIndexEntry entry = new DocumentIndexEntry(configuration.getDocumentIndexPath());
            if (entry.readFile((long) i * DocumentIndexEntry.ENTRY_SIZE)) {
                this.put(entry.getDocumentId(), entry);
            }
        }

    }
}

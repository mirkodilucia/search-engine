package it.unipi.dii.aide.mircv.document.data;

import it.unipi.dii.aide.mircv.config.Config;

import java.util.HashMap;

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
            DocumentIndexEntry entry = new DocumentIndexEntry(configuration.documentIndexPath);
            if (entry.readFile((long) i * DocumentIndexEntry.ENTRY_SIZE)) {
                this.put(entry.getDocumentId(), entry);
            }
        }

    }
}

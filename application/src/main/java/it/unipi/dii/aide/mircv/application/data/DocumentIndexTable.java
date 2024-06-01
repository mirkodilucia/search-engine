package it.unipi.dii.aide.mircv.application.data;

import java.util.HashMap;
import java.util.LinkedHashMap;

import it.unipi.dii.aide.mircv.application.config.Config;

public class DocumentIndexTable extends LinkedHashMap<Integer, DocumentIndexEntry> {

    private static DocumentIndexTable INSTANCE = null;
    private static Config configuration;

    private DocumentIndexTable() {
        super();
    }

    /**
     * Method used to instantiate the singleton object
     *
     * @return the singleton object
     */
    public static DocumentIndexTable with(Config config) {
        configuration = config;

        if (INSTANCE == null) {
            INSTANCE = new DocumentIndexTable();
        }
        return INSTANCE;
    }

    public boolean load() {
        long numDocuments = DocumentCollectionSize.getCollectionSize();

        for(int i = 0; i < numDocuments; i++) {
            DocumentIndexEntry entry = new DocumentIndexEntry(configuration);
            if (entry.readFile((long) i * DocumentIndexEntry.ENTRY_SIZE)) {
                this.put(entry.getDocumentId(), entry);
            }
            else
                return false;
        }
        return true;
    }

    public int getDocumentLength(int docId) {
        return this.get(docId).getDocumentLenght();
    }
}

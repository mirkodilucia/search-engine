package it.unipi.dii.aide.mircv.document.table;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.document.DocumentIndexState;

import java.util.LinkedHashMap;

public class DocumentIndexTable extends LinkedHashMap<Integer, DocumentIndexEntry> {

    private static DocumentIndexTable INSTANCE = null;
    private final Config config;

    private static String DOCUMENT_INDEX_FILE = "data/documents/document_index.dat";

    private static void setupPath(Config config) {
        DOCUMENT_INDEX_FILE = config.getDocumentIndexFile();
    }

    private DocumentIndexTable(Config config) {
        super();

        this.config = config;
    }

   public static DocumentIndexTable with(Config config) {
       setupPath(config);

        if (INSTANCE == null) {
            INSTANCE = new DocumentIndexTable(config);
        }
        return INSTANCE;
   }

    public boolean load() {
        long numDocuments = DocumentIndexState.getCollectionSize();

        for(int i = 0; i < numDocuments; i++) {
            DocumentIndexEntry entry = new DocumentIndexEntry(this.config, i);
            if (entry.readFile((long) i * DocumentIndexEntry.ENTRY_SIZE, DOCUMENT_INDEX_FILE)) {
                this.put(entry.getDocumentId(), entry);
            }
            else
                return false;
        }
        return true;
    }

    public int getDocumentLength(int docId) {
        DocumentIndexEntry documentIndexEntry =  this.get(docId);
        return documentIndexEntry.getDocumentLenght();
    }

}

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

    /**
     * Load the document index from the file
     * @return true if the document index is loaded correctly, false otherwise
     */
    public boolean load() {
        long numDocuments = DocumentIndexState.getCollectionSize();

        for(int i = 0; i < numDocuments + 1; i++) {
            // Print status of the loading
            if (i % 1000 == 0) {
                System.out.println("Loading document index: " + i + "/" + numDocuments);
            }

            DocumentIndexEntry entry = new DocumentIndexEntry(this.config, i, DOCUMENT_INDEX_FILE);
            if (entry.readFile((long) i * DocumentIndexEntry.ENTRY_SIZE)) {
                this.put(entry.getDocumentId(), entry);
            }
            else
                return false;
        }
        return true;
    }

    /**
     * Get the document length of a document
     * @param docId the document id
     * @return the document length
     */
    public int getDocumentLength(int docId) {
        DocumentIndexEntry documentIndexEntry = this.get(docId);
        if (documentIndexEntry == null) {
            return -1;
        }
        return documentIndexEntry.getDocumentLenght();
    }

    public String getPId(int docid) {
        return this.get(docid).getPId();
    }
}

package it.unipi.dii.aide.mircv.indexer.spimi;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.document.preprocess.FinalDocument;
import it.unipi.dii.aide.mircv.document.preprocess.InitialDocument;
import it.unipi.dii.aide.mircv.document.table.DocumentIndexEntry;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;

import java.util.HashMap;

public class Spimi extends BaseSpimi {

    private final static String COLLECTION_STATISTICS_FILE = "data/collection/collection_statistics.dat";
    private final static String DOCUMENT_INDEX_FILE = "data/documents/document_index.dat";

    private static long MEMORY_LIMIT;
    private final Config config;

    public Spimi(Config config) {
        this.config = config;
        MEMORY_LIMIT = Math.round(Runtime.getRuntime().totalMemory() * 0.2);
    }

    public static Spimi with(Config config) {
        return new Spimi(config);
    }

    public int executeSpimi() {
        boolean debugEnabled = true;
        numPostings = 0;

        while (!allDocumentsProcessed) {

            HashMap<String, PostingList> index = spimiIteration();
            boolean writeResult = saveIndexToDisk(index, debugEnabled);

            if (!writeResult) {
                System.err.println("Couldn't write index to disk.");
                return -1;
            }
        }

        this.updateIndexState();
        return numIndexes;
    }

    private void updateIndexState() {
        if(!DocumentIndexState.updateStatistics(documentId - 1,
                documentsLength
        )) {
            throw new RuntimeException("Couldn't update collection statistics.");
        }
    }


    private HashMap<String, PostingList> spimiIteration() {
        HashMap<String, PostingList> index = new HashMap<>();

        while (Runtime.getRuntime().freeMemory() > MEMORY_LIMIT) {
            // Load document
            InitialDocument initialDocument = InitialDocument.load(documentId);
            if (initialDocument == null) {
                this.onSpimiFinished();
                break;
            }

            // Process document
            FinalDocument finalDocument = initialDocument.process();
            if (finalDocument.isEmpty())
                continue;

            // Build document index entry
            DocumentIndexEntry documentIndexEntry = buildDocumentIndexEntry(finalDocument);

            // Update document length
            this.updateDocumentLength(documentIndexEntry.getDocumentLenght());
            documentIndexEntry.writeFile(DOCUMENT_INDEX_FILE);

            // Build posting list
            HashMap<String, PostingList> partialIndex = this.buildPostingList(finalDocument);
            index.putAll(partialIndex);

            this.incrementDocumentId();
        }

        return index;
    }

    private DocumentIndexEntry buildDocumentIndexEntry(FinalDocument finalDocument) {
        int documentsLength = finalDocument.getTokens().size();
        return new DocumentIndexEntry(
                this.config,
                finalDocument.getDocId(),
                documentId,
                documentsLength);
    }

    private HashMap<String, PostingList> buildPostingList(FinalDocument finalDocument) {
        HashMap<String, PostingList> index = new HashMap<>();

        for (String term : finalDocument.getTokens()) {
            if (term.isEmpty() || term.isBlank()) {
                continue;
            }

            PostingList postingList;
            if (!index.containsKey(term)) {
                postingList = new PostingList(config, term);
                index.put(term, postingList);
                continue;
            }

            postingList = index.get(term);

            boolean updated = postingList.updateOrAddPosting(documentId);
            if (updated) {
                numPostings++;
            }

            postingList.updateParameters(documentsLength);
        }

        return index;
    }
}
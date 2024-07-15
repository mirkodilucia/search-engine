package it.unipi.dii.aide.mircv.indexer.spimi;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.document.preprocess.FinalDocument;
import it.unipi.dii.aide.mircv.document.preprocess.InitialDocument;
import it.unipi.dii.aide.mircv.document.table.DocumentIndexEntry;
import it.unipi.dii.aide.mircv.indexer.model.Posting;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.utils.FileHandler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Spimi extends BaseSpimi {

    private final static String DOCUMENT_INDEX_FILE = "data/documents/document_index.dat";

    private static long MEMORY_LIMIT;

    public Spimi(Config config) {
        super(config);
        MEMORY_LIMIT = Math.round(Runtime.getRuntime().totalMemory() * 0.2);
    }

    public static Spimi with(Config config) {
        DocumentIndexState.with(config);
        return new Spimi(config);
    }

    public int executeSpimi() {
        numPostings = 0;

        spimiIteration();

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


    private int spimiIteration() {
        HashMap<String, PostingList> index = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(config.getDatasetPath()))) {
            boolean allDocumentsProcessed = false;
            int documentId = 1;
            int documentLength = 0;
            boolean writeSuccess;
            while (!allDocumentsProcessed ) {

                while (Runtime.getRuntime().freeMemory() > MEMORY_LIMIT) {
                    String line;
                    // if we reach the end of file (br.readline() -> null)
                    if ((line = br.readLine()) == null) {
                        System.out.println("all documents processed");
                        allDocumentsProcessed = true;
                        break;
                    }

                    if (line.isBlank())
                        continue;

                    String[] split = line.split("\t");

                    // Load document
                    InitialDocument initialDocument = new InitialDocument(config, split[0], split[1].replaceAll("[^\\x00-\\x7F]", ""));

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

                writeSuccess = saveIndexToDisk(index, config.debug);

                //error during data structures creation. Rollback previous operations and end algorithm
                if (!writeSuccess) {
                    System.out.println("Couldn't write index to disk.");
                    rollback();
                    return -1;
                }
                index.clear();
            }

            return numIndexes;
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void rollback() {
        FileHandler.deleteDirectory(config.getPartialIndexesDocumentsPath());
        FileHandler.deleteDirectory(config.getPartialIndexesFrequenciesPath());
        FileHandler.deleteDirectory(config.getPartialVocabularyPath());
        FileHandler.removeFile(config.getDocumentIndexFile());
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

    protected void updateOrAddPosting(int docId, PostingList postingList) {
        if (!postingList.getPostings().isEmpty()) {
            // last document inserted:
            Posting posting = postingList.getPostings().get(postingList.getPostings().size() - 1);
            //If the docId is the same I update the posting
            if (docId == posting.getDocumentId()) {
                posting.setFrequency(posting.getFrequency() + 1);
                return;
            }
        }
        // the document has not been processed (docIds are incremental):
        // create new pair and add it to the posting list
        postingList.getPostings().add(new Posting(docId, 1));

        //increment the number of postings
        numPostings++;
    }
}
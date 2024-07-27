package it.unipi.dii.aide.mircv.indexer.spimi;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.document.preprocess.FinalDocument;
import it.unipi.dii.aide.mircv.document.preprocess.InitialDocument;
import it.unipi.dii.aide.mircv.document.table.DocumentIndexEntry;
import it.unipi.dii.aide.mircv.indexer.model.Posting;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.utils.FileHandler;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

        int documents = spimiIteration();

        this.updateIndexState(documents);
        return numIndexes;
    }

    private void updateIndexState(int documentId) {
        if(!DocumentIndexState.updateStatistics(documentId - 1,
                documentsLength
        )) {
            throw new RuntimeException("Couldn't update collection statistics.");
        }
    }

    //For reading compressed tar.gz dataset
    private BufferedReader loadBuffer() throws IOException {
        BufferedReader br;

        if (config.compressedReading){

            TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(config.getCompressedCollectionPath())));
            tarInput.getNextTarEntry();
            br = new BufferedReader(new InputStreamReader(tarInput, StandardCharsets.UTF_8));
        }

        else
        {
            br = new BufferedReader(new FileReader(config.getDatasetPath()));
        }

    return br;

    }

    private int spimiIteration() {
        HashMap<String, PostingList> index = new HashMap<>();

        int documentId = 0;

        try (BufferedReader br = loadBuffer()) {
            boolean allDocumentsProcessed = false;
            boolean writeSuccess;
            while (!allDocumentsProcessed && documentId < 10) {
                int lines = 0;
                while (lines < 200) {
                //while (Runtime.getRuntime().freeMemory() > MEMORY_LIMIT) {
                    String line;
                    // if we reach the end of file (br.readline() -> null)
                    if ((line = br.readLine()) == null) {
                        System.out.println("all documents processed");
                        allDocumentsProcessed = true;
                        break;
                    }

                    lines++;
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
                    DocumentIndexEntry documentIndexEntry = buildDocumentIndexEntry(finalDocument, documentId);
                    this.documentsLength += documentIndexEntry.getDocumentLenght();

                    // Update document length
                    documentIndexEntry.writeFile(DOCUMENT_INDEX_FILE);

                    // Build posting list
                    HashMap<String, PostingList> partialIndex = this.buildPostingList(index, finalDocument, documentId);
                    index.putAll(partialIndex);

                    documentId++;

                    if((documentId % 1_000) == 0 ){
                        System.out.println("at docid: "+documentId);
                    }
                }

                writeSuccess = saveIndexToDisk(index, config.debug);

                //error during data structures creation. Rollback previous operations and end algorithm
                if (!writeSuccess) {
                    System.out.println("Couldn't write index to disk.");
                    rollback();
                    numIndexes = -1;
                    return -1;
                }

                //index.clear();
            }
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return documentId;
    }

    private void rollback() {
        FileHandler.deleteDirectory(config.getPartialIndexesDocumentsPath());
        FileHandler.deleteDirectory(config.getPartialIndexesFrequenciesPath());
        FileHandler.deleteDirectory(config.getPartialVocabularyPath());
        FileHandler.removeFile(config.getDocumentIndexFile());
    }

        private DocumentIndexEntry buildDocumentIndexEntry(FinalDocument finalDocument, int documentId) {
        int documentsLength = finalDocument.getTokens().size();
        return new DocumentIndexEntry(
                this.config,
                finalDocument.getDocId(),
                documentId,
                documentsLength);
    }

    private HashMap<String, PostingList> buildPostingList(HashMap<String, PostingList> index, FinalDocument finalDocument, int documentId) {
        for (String term : finalDocument.getTokens()) {
            if (term.isEmpty() || term.isBlank()) {
                continue;
            }

            PostingList postingList;
            if (!index.containsKey(term)) {
                postingList = new PostingList(config, term);
                index.put(term, postingList);
            }else{
                postingList = index.get(term);
            }

            boolean updated = postingList.updateOrAddPosting(documentId);
            if (!updated) {
                numPostings++;
            }

            int finalDocumentSize = finalDocument.getTokens().size();
            postingList.updateParameters(finalDocumentSize);
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
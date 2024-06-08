package it.unipi.dii.aide.mircv.application.indexer;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.VocabularyEntry;
import it.unipi.dii.aide.mircv.application.data.PostingList;

import java.io.IOException;

public class MergerWorker {

    private static Config config;
    private final int numIndexes;

    private final VocabularyEntry[] nextTerms;

    private final long[] vocEntryMemOffset;

    /**
     * Standard pathname for partial vocabulary files
     */
    private static String PATH_TO_PARTIAL_VOCABULARY;

    public void setupMergerWorker() {
        PATH_TO_PARTIAL_VOCABULARY = config.getPartialResultsConfig().getPartialVocabularyDir() + config.getVocabularyConfig().getVocabularyFile();
    }

    private MergerWorker(Config configuration, int numIndexes, VocabularyEntry[] nextTerms) {
        config = configuration;
        setupMergerWorker();
        this.numIndexes = numIndexes;
        this.nextTerms = nextTerms;
        this.vocEntryMemOffset = new long[numIndexes];
    }

    public static MergerWorker with(Config config, int numIndexes, VocabularyEntry[] nextTerms) {
        return new MergerWorker(config, numIndexes, nextTerms);
    }

    /**
     * method to process a term in a parallelized way across all the intermediate indexes:
     * - create the final posting list
     * - create the vocabulary entry for the term
     * - update term statistics in the vocabulary entry (side effect)
     *
     * @param mergerLoader: merger loader function that carries out the loading of the intermediate indexes
     * @param termToProcess: term to be processed
     * @param vocabularyEntry: vocabulary entry for new term
     * @return posting list of the processed term
     */
    public PostingList processTerm(MergerLoader mergerLoader, VocabularyEntry vocabularyEntry, String termToProcess) throws IOException {
        PostingList finalList = new PostingList(config);
        finalList.setTerm(termToProcess);

        for (int i = 0; i < vocabularyEntry.getDocumentFrequency(); i++) {
            if (nextTerms[i] == null && vocabularyEntry.getTerm().equals(termToProcess)) {
                continue;
            }

            PostingList intermediatePostingList = mergerLoader.loadList(vocabularyEntry, i);

            if(intermediatePostingList == null)
                return null;

            vocabularyEntry.updateBM25Statistics(nextTerms[i].getBM25Tf(), nextTerms[i].getBM25Dl());
            vocabularyEntry.updateStatistics(intermediatePostingList);

            finalList.appendPostings(intermediatePostingList.getPostings());
        }

        // Update the nextList array with the next term to process
        moveToNextTerm(termToProcess);

        // compute the inverse document frequency
        vocabularyEntry.calculateInverseDocumentFrequency();
        // compute the term upper bounds
        vocabularyEntry.computeUpperBounds();

        return finalList;
    }

    private void moveToNextTerm(String processedTerm) {
        // for each intermediate vocabulary
        for(int i=0; i<numIndexes; i++){
            // check if the last processed term was present in the i-th vocabulary
            if(nextTerms[i] != null && nextTerms[i].getTerm().equals(processedTerm)) {
                // last processed term was present

                // update next memory offset to be read from the i-th vocabulary
                vocEntryMemOffset[i] += VocabularyEntry.ENTRY_SIZE;

                // read next vocabulary entry from the i-th vocabulary
                long ret = nextTerms[i].readFromDisk(vocEntryMemOffset[i], PATH_TO_PARTIAL_VOCABULARY+ "_" +i);

                // check if errors occurred while reading the vocabulary entry
                if(ret == -1 || ret == 0){
                    // read ended or an error occurred
                    nextTerms[i] = null;
                }
            }
        }
    }


    /**
     * Return the minimum term of the terms to be processed in the intermediate indexes
     *  @return the next term to process
     */
    public String getMinimumTerm() {
        String term = null;

        for (int i = 0; i<numIndexes; i++) {
            if (nextTerms[i] == null)
                continue;

            String nextTerm = nextTerms[i].getTerm();
            if (term == null) {
                term = nextTerm;
                continue;
            }

            if (nextTerm.compareTo(term) < 0) {
                term = nextTerm;
            }
        }

        return term;
    }

}

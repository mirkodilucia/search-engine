package it.unipi.dii.aide.mircv.application.indexer;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.VocabularyEntry;
import it.unipi.dii.aide.mircv.application.data.PostingList;

import java.io.IOException;

public class MergerWorker {

    private static Config config;

    private MergerWorker(Config configuration) {
        config = configuration;
    }

    public static MergerWorker with(Config config) {
        return new MergerWorker(config);
    }

    /**
     * method to process a term in a parallelized way across all the intermediate indexes:
     * - create the final posting list
     * - create the vocabulary entry for the term
     * - update term statistics in the vocabulary entry (side effect)
     *
     * @param mergerLoader: merger loader function that carries out the loading of the intermediate indexes
     * @param nextTerms: next terms to be processed in the intermediate indexes
     * @param termToProcess: term to be processed
     * @param vocabularyEntry: vocabulary entry for new term
     * @return posting list of the processed term
     */
    public PostingList processTerm(MergerLoader mergerLoader, VocabularyEntry[] nextTerms, VocabularyEntry vocabularyEntry, String termToProcess) throws IOException {
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

        // compute the inverse document frequency
        vocabularyEntry.calculateInverseDocumentFrequency();
        // compute the term upper bounds
        vocabularyEntry.computeUpperBounds();

        return finalList;
    }


    /**
     * Return the minimum term of the terms to be processed in the intermediate indexes
     *  @return the next term to process
     */
    public String getMinimumTerm(VocabularyEntry[] nextTerms, int numIndexes) {
        String term = null;

        for (int i = 0; i< numIndexes; i++) {
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

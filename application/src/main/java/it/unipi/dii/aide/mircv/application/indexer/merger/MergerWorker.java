package it.unipi.dii.aide.mircv.application.indexer.merger;

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

        return finalList;
    }

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

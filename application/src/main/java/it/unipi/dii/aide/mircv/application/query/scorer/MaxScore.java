package it.unipi.dii.aide.mircv.application.query.scorer;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.PostingList;
import it.unipi.dii.aide.mircv.application.data.Vocabulary;

import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;

public class MaxScore extends Scorer {

    public MaxScore(Config config, Mode mode, ScoreFunction scoreFunction) {
        super(config, mode, scoreFunction);
    }

    public static DAAT with(Config config, Mode mode, ScoreFunction scoreFunction) {
        return null;
    }

    /**
     * method to open and to perform the first "next()" operation on posting lists to be initialized to be then scored
     * @param queryPostings: posting lists to be initialized
     */
    private void initializeList(ArrayList<PostingList> queryPostings) {
        for (PostingList postingList : queryPostings) {
            postingList.openList();
            postingList.next(config);
        }
    }

    /**
     * method to close the posting lists after the computations ended
     * @param queryPostings: posting lists to be closed
     */
    private void clean(ArrayList<PostingList> queryPostings) {
        for (PostingList postingList : queryPostings) {
            postingList.closeList();
        }
    }

    /**
     * given the array of posting list of query terms and their vocabulary entries, sort them by increasing term upper bound
     * @param queryPostings: query posting lists to be sorted
     * @return arraylist of entries of the following format: <POSTING LIST><TERM UPPER BOUND>. The arraylist is sorted by increasing TUB
     */
    private ArrayList<Map.Entry<PostingList, Double>> sortPostingListsByTerms(
            ArrayList<PostingList> queryPostings
    ) {
        PriorityQueue<Map.Entry<PostingList, Double>> sortedPostingLists = new PriorityQueue<>(queryPostings.size(), Map.Entry.comparingByValue());

        for (PostingList postingList : queryPostings) {
            double termUpperBound = 0;
            if (SCORE_FUNCTION == ScoreFunction.BM25) {
                termUpperBound = Vocabulary.with(config.getPathToVocabulary()).get(postingList.getTerm()).getMaxBM25Tf();
            }

            if (SCORE_FUNCTION == ScoreFunction.TFIDF) {
                termUpperBound = Vocabulary.with(config.getPathToVocabulary()).get(postingList.getTerm()).getMaxTfIdf();
            }

            sortedPostingLists.add(Map.entry(postingList, termUpperBound));
        }

        return new ArrayList<>(sortedPostingLists);
    }
}

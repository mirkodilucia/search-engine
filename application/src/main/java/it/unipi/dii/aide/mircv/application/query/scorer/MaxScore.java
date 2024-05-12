package it.unipi.dii.aide.mircv.application.query.scorer;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.Posting;
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

    //TODO:get non essential skipping


    /**
     * given as input the posting lists sorted by term upper bound, the index of the first essential posting list,
     * and the docid of the document to be processed with DAAT, get the partial score of the document in the essential posting lists
     * @return partial score given by essential posting lists for doc with docid equal to docToProcess
     */
    private double getPartialScore(ArrayList<Map.Entry<PostingList, Double>> sortedLists, int firstEssentialPostingListIndex, int documentToProcess, String scoringFunction)
    {
        double partialScore = 0;

        for (int i =firstEssentialPostingListIndex; i<sortedLists.size(); i++)
        {
            PostingList currentPostingList = sortedLists.get(i).getKey();

            if (currentPostingList == null)
                continue;

            Posting currentPosting = currentPostingList.getCurrentPosting();

            if (currentPosting != null)
            {
                if(currentPosting.getDocId() ==  documentToProcess)
                {
                    double idf = Vocabulary.with(config.getPathToVocabulary()).get(currentPostingList.getTerm()).getInverseDocumentFrequency();
                    partialScore += this.scoreDocument(config, currentPosting, idf);
                    currentPostingList.next(config); //TODO: check if this is correct
                }

            }

        }
        return partialScore;
    }



    /** An essential posting list is a posting list whose term upper bound
     * summed up to the term upper bounds of the preceding posting lists is >= current threshold
    */
    private int getFirstEssentialPostingListIndex(ArrayList<Map.Entry<PostingList, Double>> sortedLists, double Threshold)
    {
      double sum = 0;

      for(int i=0; i < sortedLists.size(); i++)
      {
          if(sortedLists.get(i).getKey().getCurrentPosting()==null)
              continue;

          sum = sum + sortedLists.get(i).getValue();

          if(sum > Threshold)
              return i;

      }

      return -1;
    }

    /**
     *  * Notice that the search is performed only for posting lists having index >= firstEssentialPLIndex
     *  in the arrayList given as input.
     */
    private int findNextDoc(ArrayList<Map.Entry<PostingList, Double>> sortedLists, int firstEssentialPostingListIndex) {
        int next = -1;

        for (int i = firstEssentialPostingListIndex; i < sortedLists.size(); i++) {
            Posting currentPosting = sortedLists.get(i).getKey().getCurrentPosting();

            if (currentPosting == null) {
                if (MODE == Mode.CONJUNCTIVE)
                    return -1;
                continue;
            }

            if (currentPosting != null) {
                if (MODE == Mode.CONJUNCTIVE) {
                    if (next == -1 || currentPosting.getDocId() > next) {
                        next = currentPosting.getDocId();
                    }
                } else {
                    if (next == -1 || currentPosting.getDocId() < next) {
                        next = currentPosting.getDocId();
                    }
                }
            }

        }
        return next;
    }

    /** method to move the iterators of postingsToScore to the given docid
     * @param sortedLists: posting lists that must be moved towards the given docid
     * @param docidToProcess: docid to which the iterators must be moved to
     * @return -1 if there is at least a list for which there is no docid > docidToProcess
     */
    private int movePostingScoreIterator(ArrayList<Map.Entry<PostingList,Double>> sortedLists, int docidToProcess){
        int next = docidToProcess;

        for (int i = 0; i < sortedLists.size(); i++) {
            PostingList currentPostingList = sortedLists.get(i).getKey();

            if (currentPostingList != null) {
                Posting currentPosting = currentPostingList.getCurrentPosting();

                if (currentPosting == null) {
                    return -1;
                }

                if (currentPosting.getDocId() < next) {
                    currentPosting = currentPostingList.selectPostingScoreIterator(next, config);

                    if (currentPosting == null) {
                        return -1;
                    }
                }

                if (currentPosting.getDocId() > next) {
                    next = currentPosting.getDocId();
                    i=-1;
                }
            }
        }

        return next;
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

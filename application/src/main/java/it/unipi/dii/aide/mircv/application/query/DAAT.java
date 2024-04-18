package it.unipi.dii.aide.mircv.application.query;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.Posting;
import it.unipi.dii.aide.mircv.application.data.PostingList;

import java.util.ArrayList;

public class DAAT {

    private Config config;

    private boolean isConjunctive;

    private DAAT(Config config, boolean isConjunctive){
        this.config = config;
        this.isConjunctive= isConjunctive;
    }

    public static DAAT with(Config config, boolean isConjunctive){
        return new DAAT(config, isConjunctive);
    }

    private void initializeList(ArrayList<PostingList> queryPosting){

        for (PostingList postingList : queryPosting) {
            postingList.openList();
            postingList.next(config);
        }


    }


    /** method to move the iterators of postingsToScore to the given docid
     * @param nextDocId: docid to which the iterators must be moved to
     * @return -1 if there is at least a list for which there is no docid >= docidToProcess
     */
    private int movePostingScoreIterator(int nextDocId, ArrayList<PostingList> postingScore){
        int next= nextDocId;

        for (int i = 0; i < postingScore.size(); i++) {

            PostingList currentPostingList = postingScore.get(i);

            if(currentPostingList!=null){
                Posting currentPosting = currentPostingList.getCurrentPosting();

                if(currentPosting == null){
                    return -1;
                }
                if(currentPosting.getDocId() < next){
                    currentPosting = currentPostingList.selectPostingScoreIterator(next, config);

                    if(currentPosting == null)
                        return -1;


                }
                if (currentPosting.getDocId() > next){
                    next = currentPosting.getDocId();
                    i=-1;
                }


                if(currentPosting.getDocId() == next)
                    continue;

            }
        }

        return next;
    }


    private int findNextDoc(ArrayList<PostingList> postingDocScore){
        int nextToProcessDocID = -1;

        for (PostingList postingList : postingDocScore) {

            if(postingList != null && postingList.getCurrentPosting() != null){
                int currentDocId = postingList.getCurrentPosting().getDocId();

                //Disjunction case
                if(!isConjunctive){
                    if(nextToProcessDocID == -1 || currentDocId < nextToProcessDocID)
                        nextToProcessDocID = currentDocId;

                }
                else {
                    //Conjunction case
                    if(currentDocId > nextToProcessDocID)
                        nextToProcessDocID = currentDocId;
                }
            }
        }

        if(isConjunctive)
            return movePostingScoreIterator(nextToProcessDocID, postingDocScore);

        return nextToProcessDocID;
    }

}



package it.unipi.dii.aide.mircv.cli.query.scorer;

import it.unipi.dii.aide.mircv.cli.query.enums.Mode;
import it.unipi.dii.aide.mircv.cli.query.enums.ScoreFunction;
import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.indexer.model.Posting;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;

public class DAAT extends Scorer {

    private DAAT(Config config, Mode mode, ScoreFunction scoringFunction) {
        super(config, mode, scoringFunction);
    }

    public static DAAT with(Config config, Mode mode, ScoreFunction scoringFunction){
        return new DAAT(config, mode, scoringFunction);
    }

    private void initializeList(ArrayList<PostingList> queryPosting){

        for (PostingList postingList : queryPosting) {
            postingList.openList();
            postingList.next();
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

            if (currentPostingList != null) {
                Posting currentPosting = currentPostingList.getCurrentPosting();

                if (currentPosting == null) {
                    return -1;
                }
                if (currentPosting.getDocumentId() < next) {
                    currentPosting = currentPostingList.selectPostingScoreIterator(next);

                    if(currentPosting == null)
                        return -1;

                    if (currentPosting.getDocumentId() == next) {
                        continue;
                    }

                }
                if (currentPosting.getDocumentId() > next) {
                    next = currentPosting.getDocumentId();
                    i=-1;
                }

            }
        }

        return next;
    }

    /** method to find the next docid to process in the posting lists
     * @param postingDocScore: list of posting lists
     * @return the next docid to process
     */
    private int findNextDoc(ArrayList<PostingList> postingDocScore) {
        int nextToProcessDocID = -1;

        for (PostingList postingList : postingDocScore) {

            if(postingList != null && postingList.getCurrentPosting() != null){
                int currentDocId = postingList.getCurrentPosting().getDocumentId();

                //Disjunction case
                if(MODE == Mode.DISJUNCTIVE) {
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

        if(MODE == Mode.CONJUNCTIVE)
            return movePostingScoreIterator(nextToProcessDocID, postingDocScore);

        return nextToProcessDocID;
    }

    /** method to compute the score of a document given the posting lists
     * @param documentId: id of the document to score
     * @param postingToScore: list of posting lists
     * @return the score of the document
     */
    private double computeDocumentScore(int documentId, ArrayList<PostingList> postingToScore) {
        double score = 0;

        for (PostingList postingList: postingToScore) {
            Posting posting = postingList.getCurrentPosting();
            if (posting != null && posting.getDocumentId() == documentId) {
                // Call Scoorer Algorithm
                String term = postingList.getTerm();

                Vocabulary vocabulary = Vocabulary.with(config);
                VocabularyEntry entry = vocabulary.get(term);

                double idf = entry.getIdf();

                score += this.scoreDocument(config, posting, idf);
                postingList.next();
            }
        }

        return score;
    }

    public PriorityQueue<Map.Entry<Double, Integer>> scoreQuery(
            ArrayList<PostingList> queryPostings, int k
    ) {
        initializeList(queryPostings);

        // Initialization of the MinHeap for the results
        PriorityQueue<Map.Entry<Double, Integer>> topKDocuments = new PriorityQueue<>(k, Map.Entry.comparingByKey());

        int documentToProcess = findNextDoc(queryPostings);

        while (documentToProcess != -1) {
            double documentScore = computeDocumentScore(documentToProcess, queryPostings);

            if(topKDocuments.size() != k) {
                // MinHeap is not full, the current document enters the MinHeap
                // System.out.println("heap is not full\t");
                // insert the document and its score in the MinHeap
                topKDocuments.add(new AbstractMap.SimpleEntry<>(documentScore, documentToProcess));
            } else {
                if (topKDocuments.peek() != null && documentScore > topKDocuments.peek().getKey()) {

                    topKDocuments.poll();
                    topKDocuments.add(new AbstractMap.SimpleEntry<>(documentScore, documentToProcess));
                }
            }

            documentToProcess = findNextDoc(queryPostings);
        }

        clean(queryPostings);
        return topKDocuments;
    }

    private void clean(ArrayList<PostingList> queryPostings) {
        for (PostingList postingList : queryPostings) {
            postingList.closeList();
        }
    }
}

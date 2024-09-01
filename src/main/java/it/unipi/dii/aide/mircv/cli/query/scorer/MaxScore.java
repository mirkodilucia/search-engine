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

public class MaxScore extends Scorer {

    public MaxScore(Config config, Mode mode, ScoreFunction scoreFunction) {
        super(config, mode, scoreFunction);
    }

    public static MaxScore with(Config config, Mode mode, ScoreFunction scoreFunction) {
        return new MaxScore(config, mode, scoreFunction);
    }

    /**
     * method to open and to perform the first "next()" operation on posting lists to be initialized to be then scored
     * @param queryPostings: posting lists to be initialized
     */
    private void initializeList(ArrayList<PostingList> queryPostings) {
        for (PostingList postingList : queryPostings) {
            postingList.openList();
            postingList.next();
        }
    }


    /** method to process with MaxScore algorithm a list of posting list of the query terms
     * @param queryPostings: list of postings of query terms
     * @param k: number of top k documents to be returned
     * @return returns a priority queue (of at most K elements) in the format <SCORE (Double), DOCID (Integer)> ordered by increasing score value
     */
    public PriorityQueue<Map.Entry<Double, Integer>> scoreQuery(ArrayList<PostingList> queryPostings, int k)
    {
        initializeList(queryPostings);

        PriorityQueue<Map.Entry<Double, Integer>> topKDocuments = new PriorityQueue<>(k, Map.Entry.comparingByKey());

        ArrayList<Map.Entry<PostingList, Double>> sortedLists = sortPostingListsByTerms(queryPostings);

        double threshold = -1;

        boolean newThresholdFound = true;

        int firstEssentialPostingListIndex = 0;

        while(true)
        {
            double partialScore, documentUpperBound;
            double nonEssentialPartialScoreUpperBound = 0;

            if(newThresholdFound)
            {
                firstEssentialPostingListIndex = getFirstEssentialPostingListIndex(sortedLists, threshold);

                if(firstEssentialPostingListIndex == -1)
                    break;
            }

            int nextDocToProcess = findNextDoc(sortedLists, firstEssentialPostingListIndex);

            if(nextDocToProcess == -1)
                break;

            if(this.MODE == Mode.CONJUNCTIVE)
            {
                nextDocToProcess = movePostingScoreIterator(sortedLists, nextDocToProcess);
                if(nextDocToProcess == -1)
                    break;
            }

            partialScore = getPartialScore(sortedLists, firstEssentialPostingListIndex, nextDocToProcess);

            for(int i=0; i<firstEssentialPostingListIndex; i++)
            {
                if(sortedLists.get(i) != null)
                    nonEssentialPartialScoreUpperBound += sortedLists.get(i).getValue();
            }

            documentUpperBound = partialScore + nonEssentialPartialScoreUpperBound;

            if(documentUpperBound > threshold)
            {
                double nonEssentialScores = getNonEssentialPartialScoreWithSkipping(sortedLists, firstEssentialPostingListIndex, nextDocToProcess);

                documentUpperBound = documentUpperBound - nonEssentialPartialScoreUpperBound + nonEssentialScores;

                // check if the document can enter the MinHeap
                if(documentUpperBound > threshold)
                {
                    if (topKDocuments.size() == k)
                    {
                        topKDocuments.poll();
                    }

                    topKDocuments.add(new AbstractMap.SimpleEntry<>(documentUpperBound, nextDocToProcess));

                    if(topKDocuments.size() == k)
                    {
                        threshold = documentUpperBound;
                    }
                }
            }

            // check if current threshold has been updated or not
            newThresholdFound = (threshold == documentUpperBound);
        }

        clean(queryPostings);
        return topKDocuments;
    }


    /**
     * method to score the documents in the posting lists given as input for NON-essential
     * posting lists, skipping the documents that are not equal to the docidToProcess
     * @param sortedLists: posting lists to be scored
     * @param firstNonEssentialPostingListIndex: index of the first non-essential posting list
     *                                         (i.e. the first posting list whose TUB is < current threshold)
     *                                         in the sortedLists array
     */
    private double getNonEssentialPartialScoreWithSkipping(ArrayList<Map.Entry<PostingList, Double>> sortedLists, int firstNonEssentialPostingListIndex, int documentToProcess) {
        double nonEssentialPartialScore = 0;

        for (int i=0; i < firstNonEssentialPostingListIndex; i++) {
            Map.Entry<PostingList, Double> postingListDoubleEntry = sortedLists.get(i);

            Posting nextPosting = postingListDoubleEntry.getKey().getCurrentPosting();
            if (nextPosting != null && nextPosting.getDocumentId() == documentToProcess) {
                Vocabulary vocabulary = Vocabulary.with(config);
                double idf = vocabulary
                        .get(postingListDoubleEntry.getKey().getTerm())
                        .getInverseDocumentFrequency();

                nonEssentialPartialScore += this.scoreDocument(config, nextPosting, idf);
                postingListDoubleEntry.getKey().next();
                continue;
            }

            Posting posting = postingListDoubleEntry.getKey().nextGEQ(documentToProcess);
            if (posting != null && posting.getDocumentId() == documentToProcess) {
                Vocabulary vocabulary = Vocabulary.with(config);
                double idf = vocabulary
                        .get(postingListDoubleEntry.getKey().getTerm())
                        .getInverseDocumentFrequency();

                nonEssentialPartialScore += this.scoreDocument(config, posting, idf);
                postingListDoubleEntry.getKey().next();
            }
        }

        return nonEssentialPartialScore;
    }

    /**
     * given as input the posting lists sorted by term upper bound, the index of the first essential posting list,
     * and the docid of the document to be processed with DAAT, get the partial score of the document in the essential posting lists
     * @return partial score given by essential posting lists for doc with docid equal to docToProcess
     */
    private double getPartialScore(ArrayList<Map.Entry<PostingList, Double>> sortedLists, int firstEssentialPostingListIndex, int documentToProcess)
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
                if(currentPosting.getDocumentId() ==  documentToProcess)
                {
                    Vocabulary vocabulary = Vocabulary.with(config);
                    double idf = vocabulary.get(currentPostingList.getTerm()).getInverseDocumentFrequency();
                    partialScore += this.scoreDocument(config, currentPosting, idf);
                    currentPostingList.next(); //TODO: check if this is correct
                }
            }

        }
        return partialScore;
    }



    /** An essential posting list is a posting list whose term upper bound
     * summed up to the term upper bounds of the preceding posting lists is >= current threshold
     */
    private int getFirstEssentialPostingListIndex(ArrayList<Map.Entry<PostingList, Double>> sortedLists, double currentThreshold)
    {
        double sum = 0;

        for(int i=0; i < sortedLists.size(); i++)
        {
            if(sortedLists.get(i).getKey().getCurrentPosting() == null)
                continue;

            sum = sum + sortedLists.get(i).getValue();

            if(sum > currentThreshold)
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

            if (MODE == Mode.CONJUNCTIVE) {
                if (next == -1 || currentPosting.getDocumentId() > next) {
                    next = currentPosting.getDocumentId();
                }
            } else {
                if (next == -1 || currentPosting.getDocumentId() < next) {
                    next = currentPosting.getDocumentId();
                }
            }
        }
        return next;
    }

    /** method to move the iterators of postingsToScore to the given docid
     * @param sortedLists: posting lists that must be moved towards the given docid
     * @param docIdToProcess: docid to which the iterators must be moved to
     * @return -1 if there is at least a list for which there is no docid > docidToProcess
     */
    private int movePostingScoreIterator(ArrayList<Map.Entry<PostingList,Double>> sortedLists, int docIdToProcess){
        int next = docIdToProcess;

        for (int i = 0; i < sortedLists.size(); i++) {
            PostingList currentPostingList = sortedLists.get(i).getKey();

            if (currentPostingList != null) {
                Posting currentPosting = currentPostingList.getCurrentPosting();

                if (currentPosting == null) {
                    return -1;
                }

                if (currentPosting.getDocumentId() < next) {
                    currentPosting = currentPostingList.nextGEQ(next);

                    if (currentPosting == null) {
                        return -1;
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
            String termToSearch = postingList.getTerm();
            double termUpperBound = 0;
            Vocabulary vocabulary = Vocabulary.with(config);
            VocabularyEntry entry = vocabulary.get(termToSearch);

            if (SCORE_FUNCTION == ScoreFunction.BM25) {
                termUpperBound = entry.getMaxBM25Tf();
            }

            if (SCORE_FUNCTION == ScoreFunction.TFIDF) {
                termUpperBound = entry.getMaxTfIdf();
            }

            sortedPostingLists.add(new AbstractMap.SimpleEntry<>(postingList, termUpperBound));
        }

        return new ArrayList<>(sortedPostingLists);
    }
}

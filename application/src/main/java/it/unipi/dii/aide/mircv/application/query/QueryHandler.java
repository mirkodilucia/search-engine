package it.unipi.dii.aide.mircv.application.query;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.*;
import it.unipi.dii.aide.mircv.application.query.scorer.DAAT;
import it.unipi.dii.aide.mircv.application.query.scorer.MaxScore;
import it.unipi.dii.aide.mircv.application.query.scorer.Mode;
import it.unipi.dii.aide.mircv.application.query.scorer.ScoreFunction;

import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;

public class QueryHandler {

    /**
     * Vocabulary (already loaded in memory)
     */
    private final Vocabulary vocabulary;
    private final Config config;

    private final DocumentIndexTable documentIndex;


    private QueryHandler(Config config) {
        vocabulary = Vocabulary.with(config.getPathToVocabulary());
        this.config = config;

        documentIndex = DocumentIndexTable.with(config);
    }

    public static QueryHandler with(Config config) {
        return new QueryHandler(config);
    }

    public boolean setup() {
        // load the document index
        if (!documentIndex.load())
            return false;

        return !documentIndex.isEmpty();

    }

    /**
     * load from disk the posting lists of the query tokens related to the conjunctive query type
     *
     * @param query the query document
     * @return the list of the query terms' posting lists
     */
    public ArrayList<PostingList> getConjunctiveQueryPosting(FinalDocument query) {

        ArrayList<PostingList> queryPosting = new ArrayList<>();
        ArrayList<String> queryTokens = (ArrayList<String>) query.getTokens().stream().distinct().toList();

        for (String token : queryTokens) {
            VocabularyEntry entry = vocabulary.getEntry(token);

            if (entry != null) {

                vocabulary.put(token, entry);
                queryPosting.add(new PostingList(config, entry.getTerm()));
            }

            return null;

        }

        return queryPosting;
    }


    /**
     * load from disk the posting lists of the query tokens related to the disjunctive query type
     *
     * @param query the query document
     * @return the list of the query terms' posting lists
     */
    public ArrayList<PostingList> getDisjunctiveQueryPosting(FinalDocument query) {

        ArrayList<PostingList> queryPosting = new ArrayList<>();
        ArrayList<String> queryTokens = (ArrayList<String>) query.getTokens().stream().distinct().toList();

        for (String token : queryTokens) {
            VocabularyEntry entry = vocabulary.getEntry(token);

            if (entry != null) {

                vocabulary.put(token, entry);
                queryPosting.add(new PostingList(config, entry.getTerm()));
            }

        }

        return queryPosting;
    }

    /** ?????????????????????????????
     * Lookups in the document index to retrieve pids of the top-k documents
     * @param priorityQueue The top scored documents
     * @param k number of documents to return
     * @return the ordered array of document pids
     */
    public int[] retrieveKPid(PriorityQueue<Map.Entry<Double, Integer>> priorityQueue, int k) {
        int[] output = new int[k];

        int queueSize = priorityQueue.size()-1;
        for (int i = 0; i < queueSize; i++) {
            if (priorityQueue.peek() == null)
                break;
            //TODO: check if the document id(int) is correct or pid(string)
            output[i] = documentIndex.get(priorityQueue.poll().getValue()).getDocumentId();
            i--;

        }
        return output;
    }

    /**
     * Processes a conjunctive query, computing the score for each document and returning the top-k documents
     * @param query The query string
     * @param kDoc number of documents to retrieve
     * @param scoringFunction specifies which scoring function should be used to process the query ("tfidf" or "bm25")
     * @return an array with the top-k document pids
     */
    public String[] processConjunctiveQuery(String query, int kDoc, String scoringFunction)
    {

    }

    /**
     * Processes a disjunctive query, computing the score for each document and returning the top-k documents
     * @param queryParam The query string
     * @param k number of documents to retrieve
     * @param scoreFunction specifies which scoring function should be used to process the query ("tfidf" or "bm25")
     * @return an array with the top-k document pids
     */
    public String[] processQuery(String queryParam, int k, Mode mode, ScoreFunction scoreFunction) {
        FinalDocument queryDoc = new InitialDocument(config, "0", queryParam).processDocument();
        ArrayList<PostingList> queryPosting = getConjunctiveQueryPosting(queryDoc);

        if (queryPosting == null|| queryPosting.isEmpty()) {
            return null;
        }
        PriorityQueue<Map.Entry<Double, Integer>> priorityQueue;
        if(config.MaxScoreEnabled())
            priorityQueue = DAAT.with(config, mode, scoreFunction).scoreQuery(queryPosting, k);
        else
            priorityQueue = MaxScore.with(config, mode, scoreFunction).scoreQuery(queryPosting, k);

        return retrieveKPid(priorityQueue, k);
    }
}

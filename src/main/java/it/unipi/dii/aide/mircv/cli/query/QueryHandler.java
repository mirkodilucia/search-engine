package it.unipi.dii.aide.mircv.cli.query;

import it.unipi.dii.aide.mircv.cli.query.enums.Mode;
import it.unipi.dii.aide.mircv.cli.query.enums.ScoreFunction;
import it.unipi.dii.aide.mircv.cli.query.scorer.DAAT;
import it.unipi.dii.aide.mircv.cli.query.scorer.MaxScore;
import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.document.preprocess.FinalDocument;
import it.unipi.dii.aide.mircv.document.preprocess.InitialDocument;
import it.unipi.dii.aide.mircv.document.table.DocumentIndexTable;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.PriorityQueue;

public class QueryHandler {

    private final Config config;
    private final Vocabulary vocabulary;
    private final DocumentIndexTable documentIndex;
    private Mode mode;

    private QueryHandler(Config config) {
        this.config = config;

        documentIndex = DocumentIndexTable.with(config);
        vocabulary = Vocabulary.with(config);
    }

    public static QueryHandler with(Config config) {
        return new QueryHandler(config);
    }

    public boolean setup() {
        if (!documentIndex.load())
            return false;

        return !documentIndex.isEmpty();
    }

    /**
     * Lookups in the document index to retrieve pids of the top-k documents
     * @param priorityQueue The top scored documents
     * @param k number of documents to return
     * @return the ordered array of document pids
     */
    public String[] retrieveKPid(PriorityQueue<Map.Entry<Double, Integer>> priorityQueue, int k) {
        String[] output = new String[k];

        int i = priorityQueue.size() - 1;
        while (i >= 0) {
            if (priorityQueue.peek() == null)
                break;

            int pollingValue = priorityQueue.poll().getValue();
            output[i] = documentIndex.getPId(pollingValue);
            i--;
        }
        return output;
    }

    /**
     * Processes a disjunctive query, computing the score for each document and returning the top-k documents
     * @param queryParam The query string
     * @param maxDocumentResult number of documents to retrieve
     * @param scoreFunction specifies which scoring function should be used to process the query ("tfidf" or "bm25")
     * @return an array with the top-k document pids
     */
    public String[] processQuery(String queryParam, int maxDocumentResult, Mode mode, ScoreFunction scoreFunction) {
        FinalDocument queryDoc = new InitialDocument(config,"query", queryParam).process();
        ArrayList<PostingList> queryPosting = getQueryPosting(queryDoc);

        if (queryPosting == null || queryPosting.isEmpty()) {
            return null;
        }

        this.mode = mode;

        PriorityQueue<Map.Entry<Double, Integer>> priorityQueue;
        if(config.scorerConfig.isMaxScoreEnabled())
            priorityQueue = DAAT.with(config, mode, scoreFunction).scoreQuery(queryPosting, maxDocumentResult);
        else
            priorityQueue = MaxScore.with(config, mode, scoreFunction).scoreQuery(queryPosting, maxDocumentResult);

        return retrieveKPid(priorityQueue, maxDocumentResult);
    }

    /** Get the posting list of the query terms
     * @param queryDoc The query document object
     * @return the posting list of the query terms
     */
    public ArrayList<PostingList> getQueryPosting(FinalDocument queryDoc) {
        ArrayList<PostingList> queryPosting = new ArrayList<>();
        ArrayList<String> queryTerms = new ArrayList<>(queryDoc.getTokens().stream().distinct().toList());

        for (String queryTerm : queryTerms) {
            VocabularyEntry entry = vocabulary.getEntry(queryTerm);

            if (entry == null) {
                if (mode == Mode.CONJUNCTIVE)
                    return null;

                continue;
            }

            vocabulary.put(queryTerm, entry);
            queryPosting.add(new PostingList(config, entry.getTerm()));
        }

        return queryPosting;
    }
}

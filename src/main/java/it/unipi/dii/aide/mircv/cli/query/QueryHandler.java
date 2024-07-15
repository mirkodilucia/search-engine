package it.unipi.dii.aide.mircv.cli.query;

import it.unipi.dii.aide.mircv.cli.query.enums.Mode;
import it.unipi.dii.aide.mircv.cli.query.enums.ScoreFunction;
import it.unipi.dii.aide.mircv.cli.query.scorer.DAAT;
import it.unipi.dii.aide.mircv.cli.query.scorer.MaxScore;
import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.document.preprocess.FinalDocument;
import it.unipi.dii.aide.mircv.document.preprocess.InitialDocument;
import it.unipi.dii.aide.mircv.document.table.DocumentIndexTable;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;

import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;

public class QueryHandler {

    private final Config config;
    private final Vocabulary vocabulary;
    private final Mode mode;
    private final DocumentIndexTable documentIndex;

    private QueryHandler(Config config, Mode mode) {
        this.config = config;
        this.mode = mode;

        documentIndex = DocumentIndexTable.with(config);
        vocabulary = Vocabulary.with(config);
    }

    public static QueryHandler with(Config config, Mode mode) {
        return new QueryHandler(config, mode);
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
            output[i] = documentIndex.get(pollingValue).getPId();
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
        PriorityQueue<Map.Entry<Double, Integer>> priorityQueue;
        if(config.scorerConfig.isMaxScoreEnabled())
            priorityQueue = DAAT.with(config, mode, scoreFunction).scoreQuery(queryPosting, maxDocumentResult);
        else
            priorityQueue = MaxScore.with(config, mode, scoreFunction).scoreQuery(queryPosting, maxDocumentResult);

        return retrieveKPid(priorityQueue, maxDocumentResult);
    }

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

package it.unipi.dii.aide.mircv.performance;

import it.unipi.dii.aide.mircv.cli.query.QueryHandler;
import it.unipi.dii.aide.mircv.cli.query.enums.Mode;
import it.unipi.dii.aide.mircv.cli.query.enums.ScoreFunction;
import it.unipi.dii.aide.mircv.cli.query.scorer.DAAT;
import it.unipi.dii.aide.mircv.cli.query.scorer.MaxScore;
import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.document.preprocess.FinalDocument;
import it.unipi.dii.aide.mircv.document.preprocess.InitialDocument;
import it.unipi.dii.aide.mircv.document.table.DocumentIndexTable;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;

public class Performances
{
    protected static Config config;

    private static final String QUERIES_PATH = "data/queries/queries.dev.tsv";
    private static final String TREC_EVAL_RESULTS_PATH = "data/queries/search_engine_results_" + "bm25" + ".txt";

    private static final int k = 100;
    private static final boolean maxScore = false;
    private static final boolean isTrecEvalTest = false;

    private static boolean storeResults(String topicId, PriorityQueue<Map.Entry<Double, Integer>> priorityQueue)
    {
        String results;
        int i = priorityQueue.size();
        DocumentIndexTable documentIndex = DocumentIndexTable.with(config);

        try (BufferedWriter statisticsBuffer = new BufferedWriter(new FileWriter(TREC_EVAL_RESULTS_PATH, true))) {
            while (priorityQueue.peek() != null) {
                Map.Entry<Double, Integer> entry = priorityQueue.poll();
                results = topicId + "\tQ0\t" + documentIndex.get(entry.getValue()) + "\t" + i + "\t" + entry.getKey() + "\t" + "RUN-1\n";
                statisticsBuffer.write(results);
                i--;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    public static void main(String [] args)
    {
        config = new Config();

        System.out.println("Begin");
        QueryHandler queryHandler = QueryHandler.with(config, Mode.DISJUNCTIVE);
        boolean success = queryHandler.setup();

        if (!success)
        {
            System.out.println("Error in setup");
            return;
        }

        try(BufferedReader br = Files.newBufferedReader(Paths.get(QUERIES_PATH), StandardCharsets.UTF_8))
        {
            System.out.println("Starting processing queries");

            String line;
            long sumResponseTime = 0;
            int nQueries = 0;
            ArrayList<Long> responseTimes = new ArrayList<>();

            while(true)
            {
                if ((line = br.readLine()) == null)
                {
                    System.out.println("all queries processed");
                    break;
                }

                if (line.isBlank())
                    continue;

                // split of the line in the format <qid>\t<text>
                String[] split = line.split("\t");

                if(split.length != 2)
                    continue;


                InitialDocument document = new InitialDocument(config, split[0], split[1].replaceAll("[^\\x00-\\x7F]", ""));
                // Perform text preprocessing on the document
                FinalDocument processedQuery = document.process();

                // load the posting lists of the tokens
                ArrayList<PostingList> queryPostings = queryHandler.getQueryPosting(processedQuery);
                if(queryPostings == null || queryPostings.isEmpty()){
                    continue;
                }

                PriorityQueue<Map.Entry<Double, Integer>> priorityQueue;

                long start = System.currentTimeMillis();

                if (!maxScore) {
                    DAAT scorerDAAT;
                    scorerDAAT = DAAT.with(config, Mode.DISJUNCTIVE, ScoreFunction.BM25);
                    priorityQueue = scorerDAAT.scoreQuery(queryPostings, k);
                } else {
                    MaxScore scorerMaxScore;
                    scorerMaxScore = MaxScore.with(config, Mode.DISJUNCTIVE, ScoreFunction.BM25);
                    priorityQueue = scorerMaxScore.scoreQuery(queryPostings, k);
                }

                long stop = System.currentTimeMillis();
                nQueries++;
                sumResponseTime += (stop - start);
                responseTimes.add(stop - start);

                if (isTrecEvalTest)
                    if (!storeResults(String.valueOf(processedQuery.getPid()), priorityQueue))
                        System.out.println("Error encountered while writing results");

            }

            double mean = sumResponseTime / (double) nQueries;
            double standardDeviation = 0.0;
            for (long num : responseTimes) {
                standardDeviation += Math.pow(num - mean, 2);
            }
            standardDeviation = Math.sqrt(standardDeviation / nQueries);
            System.out.println("Query mean response time is: " + sumResponseTime / nQueries + " milliseconds, with a standard deviation of " + standardDeviation);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Performance test failed");
        }
    }
}

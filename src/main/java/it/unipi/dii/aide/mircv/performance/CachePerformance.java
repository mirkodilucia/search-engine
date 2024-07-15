package it.unipi.dii.aide.mircv.performance;


import it.unipi.dii.aide.mircv.cli.query.QueryHandler;
import it.unipi.dii.aide.mircv.cli.query.enums.Mode;
import it.unipi.dii.aide.mircv.cli.query.enums.ScoreFunction;
import it.unipi.dii.aide.mircv.cli.query.scorer.DAAT;
import it.unipi.dii.aide.mircv.cli.query.scorer.MaxScore;
import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.document.preprocess.FinalDocument;
import it.unipi.dii.aide.mircv.document.preprocess.InitialDocument;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;

public class CachePerformance {

    private static final String QUERIES_PATH = "data/queries/queries.dev.tsv";
    private static final String RESULT_PATH = "data/queries/results.txt";
    private static final String STAT_PATH = "data/queries/stats.txt";

    private static boolean maxScore = true;
    private static int k = 100;
    private static long timeNoCache = 0;
    private static long timeCache = 0;
    private static long totQueries = 0;
    private static ScoreFunction SCORING_FUNCTION = ScoreFunction.TFIDF;

    private static void perfomanceQueries()
    {
        Config config = new Config();

        QueryHandler queryHandler = QueryHandler.with(config, Mode.DISJUNCTIVE);
        boolean success = queryHandler.setup();

        try ( BufferedReader br = Files.newBufferedReader(Paths.get(QUERIES_PATH), StandardCharsets.UTF_8);
              BufferedWriter resultBuffer = new BufferedWriter(new FileWriter(RESULT_PATH, true));
            )
        {
            System.out.println("Starting");

            String line;

            while((line = br.readLine()) != null)
            {
                // if the line is empty we process the next line
                if (line.isBlank())
                    continue;

                // split of the line in the format <qid>\t<text>
                String[] split = line.split("\t");

                if (split.length != 2)
                    continue;

                InitialDocument document = new InitialDocument(config, split[0], split[1].replaceAll("[^\\x00-\\x7F]", ""));
                // Perform text preprocessing on the document
                FinalDocument processedQuery = document.process();
                PriorityQueue<Map.Entry<Double, Integer>> priorityQueue;

                // load the posting lists of the tokens
                ArrayList<PostingList> queryPostings = queryHandler.getQueryPosting(processedQuery);
                if(queryPostings == null || queryPostings.isEmpty()){
                    continue;
                }

                long start = System.currentTimeMillis();

                if (!maxScore)
                {
                    DAAT scorerDAAT;
                    scorerDAAT = DAAT.with(config, Mode.DISJUNCTIVE, SCORING_FUNCTION);
                    priorityQueue = scorerDAAT.scoreQuery(queryPostings, k);
                }
                else
                {
                    MaxScore scorerMaxScore;
                    scorerMaxScore = MaxScore.with(config, Mode.DISJUNCTIVE, SCORING_FUNCTION);
                    priorityQueue = scorerMaxScore.scoreQuery(queryPostings, k);
                }

                long stop = System.currentTimeMillis();

                if (priorityQueue.isEmpty())
                    continue;

                long responseTime = stop - start;

                System.out.println("Response time for query " + processedQuery.getPid() + "is: " + responseTime + " milliseconds");

                resultBuffer.write(processedQuery.getPid() + '\t' + responseTime + '\t' + " no cache " + '\n');

                timeNoCache += responseTime;

                //Cache part
                long startCache = System.currentTimeMillis();

                ArrayList<PostingList> queryPostingsCache = queryHandler.getQueryPosting(processedQuery);
                if(queryPostingsCache == null || queryPostingsCache.isEmpty()){
                    continue;
                }

                if (!maxScore)
                {
                    DAAT scorerDAAT;
                    scorerDAAT = DAAT.with(config, Mode.DISJUNCTIVE, SCORING_FUNCTION);
                    priorityQueue = scorerDAAT.scoreQuery(queryPostings, k);
                }
                else
                {
                    MaxScore scorerMaxScore;
                    scorerMaxScore = MaxScore.with(config, Mode.DISJUNCTIVE, SCORING_FUNCTION);
                    priorityQueue = scorerMaxScore.scoreQuery(queryPostings, k);
                }

                long stopCache = System.currentTimeMillis();

                if (priorityQueue.isEmpty())
                    continue;

                long responseTimeCache = stopCache - startCache;

                System.out.println("Response time for query " + processedQuery.getPid() + "is: " + responseTimeCache + " milliseconds");

                resultBuffer.write('\n');
                resultBuffer.write(processedQuery.getPid() + '\t' + responseTimeCache + '\t' + " cache " + '\n');

                timeCache += responseTime;

                Vocabulary.clearCache();

                totQueries++;

            }
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        Config config = new Config();

        System.out.println("Starting");
        QueryHandler queryHandler = QueryHandler.with(config, Mode.DISJUNCTIVE);

        boolean setupSuccess = queryHandler.setup();

        if (!setupSuccess) {
            System.out.println("Error in setup");
            return;
        }

        System.out.println("Starting tests");


        perfomanceQueries();

        SCORING_FUNCTION = ScoreFunction.BM25;

        perfomanceQueries();

        try (
                BufferedWriter statBuffer = new BufferedWriter(new FileWriter(STAT_PATH, true));
        ) {
            double avgNoCache = (double) timeNoCache / (double) totQueries;
            double avgCache = (double) timeCache / (double) totQueries;

            statBuffer.write("AVG time: " + '\t' + avgNoCache);
            statBuffer.write('\n');
            statBuffer.write("AVG time with caching: " + '\t' + avgCache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

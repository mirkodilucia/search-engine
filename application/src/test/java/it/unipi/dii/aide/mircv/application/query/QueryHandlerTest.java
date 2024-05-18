package it.unipi.dii.aide.mircv.application.query;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.DocumentIndexTable;
import it.unipi.dii.aide.mircv.application.data.Vocabulary;
import it.unipi.dii.aide.mircv.application.data.VocabularyEntry;
import it.unipi.dii.aide.mircv.application.query.scorer.DAAT;
import it.unipi.dii.aide.mircv.application.query.scorer.Mode;
import it.unipi.dii.aide.mircv.application.query.scorer.ScoreFunction;
import it.unipi.dii.aide.mircv.application.data.DocumentCollectionSize;
import it.unipi.dii.aide.mircv.application.query.scorer.MaxScore;
import it.unipi.dii.aide.mircv.application.data.PostingList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryHandlerTest
{

    private static Config config;

    private static DocumentIndexTable docIndex;

    @BeforeAll
    public static void setup() {
        config = new Config();
        config.setPathToVocabulary("../test/data/queryHandler/vocabulary");
        config.setPathToBlockDescriptors("../test/data/queryHandler/blockDescriptors");
        config.setDocumentIndexPath("../test/data/queryHandler/documentIndex");
        config.setPathToInvertedIndexDocs("../test/data/queryHandler/invertedIndexDocs");
        config.setPathToInvertedIndexFreq("../test/data/queryHandler/invertedIndexFreqs");

        docIndex = DocumentIndexTable.with(config);
        VocabularyEntry.setBlockDescriptorsPath("../test/data/queryHandler/blockDescriptors");
    }

    @BeforeEach
    public void init() {
        DocumentCollectionSize.setTotalDocumentLen(61);
        DocumentCollectionSize.setCollectionSize(8);

        Vocabulary.unsetInstance();
        Vocabulary v = Vocabulary.with(config.getPathToVocabulary());

        boolean vocabularyLoaded = v.readFromDisk();
        assertTrue(vocabularyLoaded);

        boolean tableLoaded = docIndex.load();
        assertTrue(tableLoaded);
    }

    public Object[] reformatQueue(PriorityQueue<Map.Entry<Double, Integer>> queue) {
        //arraylist storing the result
        ArrayList<AbstractMap.SimpleEntry<Double, Integer>> returnList = new ArrayList<>();

        //get array from queue
        Object[] queueArray = queue.toArray();

        //populate array list
        try {
            for (int i = 0; i < queueArray.length; i++)
                returnList.add((AbstractMap.SimpleEntry<Double, Integer>) queueArray[i]);

        }catch (ClassCastException e){
            e.printStackTrace();
        }


        //sort arraylist since there is no guarantee of order of priority queue after making it an array
        returnList.sort(Map.Entry.comparingByKey());

        //cast to array
        return returnList.toArray();
    }

    @ParameterizedTest
    @MethodSource("getTFIDFParameters")
    void testMaxScoreTFIDF(int k, ArrayList<PostingList> postings, boolean isConjunctive, PriorityQueue<Map.Entry<Double, Integer>> expected ){
        config.setMaxScoreEnabled(true);
        config.setCompression(true);
        config.setStemStopRemoval(false);

        Mode mode = isConjunctive ? Mode.CONJUNCTIVE : Mode.DISJUNCTIVE;
        MaxScore scorer = MaxScore.with(config, mode, ScoreFunction.TFIDF);

        assertArrayEquals(reformatQueue(expected), reformatQueue(scorer.scoreQuery(postings, k)));
    }

    @ParameterizedTest
    @MethodSource("getBM25Parameters")
    void testMaxScoreBM25(int k, ArrayList<PostingList> postings, boolean isConjunctive, PriorityQueue<Map.Entry<Double, Integer>> expected) {
        config.setMaxScoreEnabled(true);
        config.setCompression(true);
        config.setStemStopRemoval(false);

        Mode mode = isConjunctive ? Mode.CONJUNCTIVE : Mode.DISJUNCTIVE;
        MaxScore scorer = MaxScore.with(config, mode, ScoreFunction.BM25);

        assertArrayEquals(reformatQueue(expected), reformatQueue(scorer.scoreQuery(postings, k)));
    }

    @ParameterizedTest
    @MethodSource("getTFIDFParameters")
    void testDAATTFIDF(int k, ArrayList<PostingList> postings, boolean isConjunctive, PriorityQueue<Map.Entry<Double, Integer>> expected) {
        config.setMaxScoreEnabled(false);
        config.setCompression(true);
        config.setStemStopRemoval(false);

        Mode mode = isConjunctive ? Mode.CONJUNCTIVE : Mode.DISJUNCTIVE;
        DAAT scorer = DAAT.with(config, mode, ScoreFunction.BM25);

        assertArrayEquals(reformatQueue(expected), reformatQueue(scorer.scoreQuery(postings, k)));
    }

    @ParameterizedTest
    @MethodSource("getBM25Parameters")
    void testDAATBM25(int k, ArrayList<PostingList> postings, boolean isConjunctive, PriorityQueue<Map.Entry<Double, Integer>> expected) {
        config.setMaxScoreEnabled(false);
        config.setCompression(true);
        config.setStemStopRemoval(false);

        Mode mode = isConjunctive ? Mode.CONJUNCTIVE : Mode.DISJUNCTIVE;
        DAAT scorer = DAAT.with(config, mode, ScoreFunction.BM25);

        assertArrayEquals(reformatQueue(expected), reformatQueue(scorer.scoreQuery(postings, k)));
    }

    @AfterAll
    static void cleanup() {
        //FileUtils.removeFile(config.getPathToVocabulary());
        //FileUtils.removeFile(config.getDocumentIndexPath());
        //FileUtils.removeFile(config.getPathToInvertedIndexDocs());
        //FileUtils.removeFile(config.getPathToInvertedIndexFreqs());
        //FileUtils.removeFile(config.getBlockDescriptorsPath());
    }

    public static Stream<Arguments> getBM25Parameters() {
        PriorityQueue<Map.Entry<Double, Integer>> expectedResultsAnotherExampleConjBM25 = new PriorityQueue<>(3, Map.Entry.comparingByKey());
        PriorityQueue<Map.Entry<Double, Integer>> expectedResultsAnotherExampleDisBM25 = new PriorityQueue<>(3, Map.Entry.comparingByKey());
        PriorityQueue<Map.Entry<Double, Integer>> expectedResultsExampleDisBM25 = new PriorityQueue<>(3, Map.Entry.comparingByKey());
        PriorityQueue<Map.Entry<Double, Integer>> expectedResultsExampleConjBM25 = new PriorityQueue<>(3, Map.Entry.comparingByKey());
        PriorityQueue<Map.Entry<Double, Integer>> expectedResultsEmpty = new PriorityQueue<>(3, Map.Entry.comparingByKey());

        //queue for query "another example" conjunctive mode with bm25
        expectedResultsAnotherExampleConjBM25.add(new AbstractMap.SimpleEntry<>(0.2582940702253402, 8));
        expectedResultsAnotherExampleConjBM25.add(new AbstractMap.SimpleEntry<>(0.38158664142011345, 2));


        //queue for query "another example" disjunctive mode with bm25
        expectedResultsAnotherExampleDisBM25.add(new AbstractMap.SimpleEntry<>(0.1123005090598549, 3));
        expectedResultsAnotherExampleDisBM25.add(new AbstractMap.SimpleEntry<>(0.38158664142011345, 2));
        expectedResultsAnotherExampleDisBM25.add(new AbstractMap.SimpleEntry<>(0.2582940702253402, 8));

        //queue for query "example" disjunctive mode with bm25
        expectedResultsExampleDisBM25.add(new AbstractMap.SimpleEntry<>(0.09030875025937561, 5));
        expectedResultsExampleDisBM25.add(new AbstractMap.SimpleEntry<>(0.1123005090598549, 3));
        expectedResultsExampleDisBM25.add(new AbstractMap.SimpleEntry<>(0.09661547190697509, 2));

        //queue for query "example" conjunctive mode with bm25
        expectedResultsExampleConjBM25.add(new AbstractMap.SimpleEntry<>(0.09030875025937561, 5));
        expectedResultsExampleConjBM25.add(new AbstractMap.SimpleEntry<>(0.1123005090598549, 3));
        expectedResultsExampleConjBM25.add(new AbstractMap.SimpleEntry<>(0.09661547190697509, 2));

        //postings for query "another example"
        ArrayList<PostingList> queryPostingsAnotherExample = new ArrayList<>(Arrays.stream(
                new PostingList[]{new PostingList(config, "example"), new PostingList(config, "another")}).toList());

        //postings for query "example"
        ArrayList<PostingList> queryPostingsExample = new ArrayList<>(Arrays.stream(
                new PostingList[]{new PostingList(config, "example")}).toList());

        //postings for query "simple example"
        ArrayList<PostingList> queryPostingsSimpleExample = new ArrayList<>(Arrays.stream(
                new PostingList[]{new PostingList(config, "example"), new PostingList(config, "simple")}).toList());

        return Stream.of(Arguments.arguments(3, queryPostingsAnotherExample, true, expectedResultsAnotherExampleConjBM25),
                Arguments.arguments(3, queryPostingsAnotherExample, false, expectedResultsAnotherExampleDisBM25),
                Arguments.arguments(3, queryPostingsExample, false, expectedResultsExampleDisBM25),
                Arguments.arguments(3, queryPostingsExample, true, expectedResultsExampleConjBM25),
                Arguments.arguments(3, queryPostingsSimpleExample, true, expectedResultsEmpty)
        );
    }

    public static Stream<Arguments> getTFIDFParameters() {
        PriorityQueue<Map.Entry<Double, Integer>> expectedResultsAnotherExampleConjTfidf = new PriorityQueue<>(3, Map.Entry.comparingByKey());
        PriorityQueue<Map.Entry<Double, Integer>> expectedResultsAnotherExampleDisTfidf = new PriorityQueue<>(3, Map.Entry.comparingByKey());
        PriorityQueue<Map.Entry<Double, Integer>> expectedResultsExampleDisTfidf = new PriorityQueue<>(3, Map.Entry.comparingByKey());
        PriorityQueue<Map.Entry<Double, Integer>> expectedResultsExampleConjTfidf = new PriorityQueue<>(3, Map.Entry.comparingByKey());
        PriorityQueue<Map.Entry<Double, Integer>> expectedResultsEmpty = new PriorityQueue<>(3, Map.Entry.comparingByKey());

        //queue for query "another example" conjunctive mode with tfidf
        expectedResultsAnotherExampleConjTfidf.add(new AbstractMap.SimpleEntry<>(0.8061799739838872, 2));
        expectedResultsAnotherExampleConjTfidf.add(new AbstractMap.SimpleEntry<>(0.9874180905628003, 8));

        //queue for query "another example" disjunctive mode with tfidf
        expectedResultsAnotherExampleDisTfidf.add(new AbstractMap.SimpleEntry<>(0.8061799739838872, 2));
        expectedResultsAnotherExampleDisTfidf.add(new AbstractMap.SimpleEntry<>(0.30150996489407533, 6));
        expectedResultsAnotherExampleDisTfidf.add(new AbstractMap.SimpleEntry<>(0.9874180905628003, 8));

        //queue for query "example" disjunctive mode with tfidf
        expectedResultsExampleDisTfidf.add(new AbstractMap.SimpleEntry<>(0.2041199826559248, 5));
        expectedResultsExampleDisTfidf.add(new AbstractMap.SimpleEntry<>(0.2041199826559248, 3));
        expectedResultsExampleDisTfidf.add(new AbstractMap.SimpleEntry<>(0.30150996489407533, 6));

        //queue for query "example" conjunctive mode with tfidf
        expectedResultsExampleConjTfidf.add(new AbstractMap.SimpleEntry<>(0.2041199826559248, 5));
        expectedResultsExampleConjTfidf.add(new AbstractMap.SimpleEntry<>(0.2041199826559248, 3));
        expectedResultsExampleConjTfidf.add(new AbstractMap.SimpleEntry<>(0.30150996489407533, 6));

        //postings for query "another example"
        ArrayList<PostingList> queryPostingsAnotherExample = new ArrayList<>(Arrays.stream(
                new PostingList[]{new PostingList(config, "example"), new PostingList(config, "another")}).toList());

        //postings for query "example"
        ArrayList<PostingList> queryPostingsExample = new ArrayList<>(Arrays.stream(
                new PostingList[]{new PostingList(config, "example")}).toList());

        //postings for query "simple example"
        ArrayList<PostingList> queryPostingsSimpleExample = new ArrayList<>(Arrays.stream(
                new PostingList[]{new PostingList(config, "example"), new PostingList(config, "simple")}).toList());

        return Stream.of(Arguments.arguments(3, queryPostingsAnotherExample, true, expectedResultsAnotherExampleConjTfidf),
                Arguments.arguments(3, queryPostingsAnotherExample, false, expectedResultsAnotherExampleDisTfidf),
                Arguments.arguments(3, queryPostingsExample, false, expectedResultsExampleDisTfidf),
                Arguments.arguments(3, queryPostingsExample, true, expectedResultsExampleConjTfidf),
                Arguments.arguments(3, queryPostingsSimpleExample, true, expectedResultsEmpty)
        );
    }
}

package it.unipi.dii.aide.mircv.cli;

import it.unipi.dii.aide.mircv.cli.query.QueryHandler;
import it.unipi.dii.aide.mircv.cli.query.enums.Mode;
import it.unipi.dii.aide.mircv.cli.query.enums.ScoreFunction;
import it.unipi.dii.aide.mircv.config.model.Config;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class QueryParser {

    private static final int MAX_DOCUMENT_RESULT = 10;

    private static QueryHandler queryHandler;
    private final Scanner scanner;
    private final Config config;

    /**
     * Constructor of the QueryParser class that takes the configuration object and the scanner
     * @param config
     * @param scanner
     */
    public QueryParser(Config config, Scanner scanner) {
        this.config = config;
        this.scanner = scanner;

        queryHandler = QueryHandler.with(config);
        queryHandler.setup();
    }

    /**
     * Process the query
     * @param queryParams the query parameters
     * @return true if the query is processed correctly, false otherwise
     */
    public boolean processQuery(String[] queryParams) {
        if (CommandParser.isConjunctiveMode(queryParams)) {
            return parseConjuntiveQuery(queryParams);
        }

        if (CommandParser.isDisjunctiveMode(queryParams)) {
            return parseDisjunctiveQuery(queryParams);
        }

        System.out.println("The query you entered is in invalid format.");
        return false;
    }

    /**
     * Parse the conjunctive query
     * @param queryParams the query parameters
     * @return true if the query is processed correctly, false otherwise
     */
    private boolean parseConjuntiveQuery(String[] queryParams) {
        String[] documents;
        ScoreFunction scoreFunction = askForScoringFunction();

        queryHandler = QueryHandler.with(config);

        documents = queryHandler.processQuery(queryParams[0], MAX_DOCUMENT_RESULT, Mode.CONJUNCTIVE, scoreFunction);
        showDocumentsResults(documents);
        return true;
    }

    /**
     * Parse the disjunctive query
     * @param queryParams the query parameters
     * @return true if the query is processed correctly, false otherwise
     */
    private boolean parseDisjunctiveQuery(String[] queryParams) {
        String[] documents;
        ScoreFunction scoreFunction = askForScoringFunction();

        documents = queryHandler.processQuery(queryParams[0], MAX_DOCUMENT_RESULT, Mode.DISJUNCTIVE, scoreFunction);
        showDocumentsResults(documents);
        return true;
    }

    /**
     * Show the documents results of the query
     * @param documents the documents
     */
    private static void showDocumentsResults(String[] documents) {
        if (documents == null || documents.length == 0) {
            System.out.println("No documents found with the term provided");
            return;
        }

        System.out.println("Term found in these documents:");
        for (String document : Arrays.stream(documents)
                .filter(Objects::nonNull)
                .toArray(String[]::new)
        ) {
            System.out.println(document);
        }
    }

    /**
     * Ask the user for the scoring function to apply
     * @return the scoring function
     */
    private ScoreFunction askForScoringFunction() {
        System.out.println("Which scoring function would you like to apply?\nType tfidf or bm25");
        String response = scanner.nextLine();

        if (CommandParser.isTfIdfScoring(response)) {
            return ScoreFunction.TFIDF;
        }

        if (CommandParser.isBM25Scoring(response)) {
            return ScoreFunction.BM25;
        }

        System.out.println("Invalid scoring function. Please try again.");
        return askForScoringFunction();
    }
}

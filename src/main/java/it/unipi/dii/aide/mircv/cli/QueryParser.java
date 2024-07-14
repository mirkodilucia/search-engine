package it.unipi.dii.aide.mircv.cli;

import it.unipi.dii.aide.mircv.cli.query.QueryHandler;
import it.unipi.dii.aide.mircv.cli.query.enums.Mode;
import it.unipi.dii.aide.mircv.cli.query.enums.ScoreFunction;
import it.unipi.dii.aide.mircv.config.Config;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class QueryParser {

    private static final int MAX_DOCUMENT_RESULT = 10;

    private static QueryHandler queryHandler;
    private final Scanner scanner;
    private final Config config;

    public QueryParser(Config config, Scanner scanner) {
        this.config = config;
        this.scanner = scanner;
    }

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

    private boolean parseConjuntiveQuery(String[] queryParams) {
        String[] documents;
        ScoreFunction scoreFunction = askForScoringFunction();

        queryHandler = QueryHandler.with(config, Mode.CONJUNCTIVE);
        queryHandler.setup();

        documents = queryHandler.processQuery(queryParams[0], MAX_DOCUMENT_RESULT, Mode.CONJUNCTIVE, scoreFunction);
        showDocumentsResults(documents);
        return true;
    }

    private boolean parseDisjunctiveQuery(String[] queryParams) {
        String[] documents;
        ScoreFunction scoreFunction = askForScoringFunction();

        queryHandler = QueryHandler.with(config, Mode.DISJUNCTIVE);
        queryHandler.setup();

        documents = queryHandler.processQuery(queryParams[0], MAX_DOCUMENT_RESULT, Mode.DISJUNCTIVE, scoreFunction);
        showDocumentsResults(documents);
        return true;
    }

    private static void showDocumentsResults(String[] documents) {
        if (documents.length == 0) {
            System.out.println("No documents found.");
            return;
        }

        System.out.println("Documents found:");
        for (String document : Arrays.stream(documents)
                .filter(Objects::nonNull)
                .toArray(String[]::new)
        ) {
            System.out.println(document);
        }
    }

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

package it.unipi.dii.aide.mircv.application;

import java.util.Scanner;
import it.unipi.dii.aide.mircv.application.cli.CommandParser;

public class CommandLine {

    private static Scanner scanner;

    public static void main(String[] args) {
        System.out.println("****** SEARCH ENGINE ******");
        System.out.println("Starting...");
        //check if setup of data structures was successful
        //boolean setupSuccess = QueryProcesser.setupProcesser();

        /*
        if (!setupSuccess) {
            System.out.println("Error in setup of this service. Shutting down...");
            return;
        }


        if(args.length > 0){
            if(args[0].equals("-maxscore")) {
                Flags.setMaxScore(true);
            }else{
                System.out.println("Flag not recognized");
            }
        }
        */

        scanner = new Scanner(System.in);


        System.out.println(
                "What are you looking for? " + """
                Please insert a query specifying your preferred mode:\s
                -c for conjunctive mode or -d for disjunctive mode. Here's an example:\s
                This is a query example -c \s
                Type "help" to get help or "break" to terminate the service""");

        while (true) {
            String query = scanner.nextLine();
            if (!CommandParser.isValidQuery(query)) {
                System.out.println("The query you entered is empty.");
                continue;
            }

            String[] queryParams = CommandParser.parseQuery(query);
            if (queryParams.length == 1) {
                if (CommandParser.isBreakCommand(queryParams)) {
                    break;
                }

                if (CommandParser.isHelpCommand(queryParams)) {
                    continue;
                }

                System.out.println("The query you entered is in invalid format.");
                continue;
            }

            if (queryParams.length > 1) {
                processQuery(queryParams);
            }
        }
    }

    private static boolean processQuery(String[] queryParams) {
        String[] documents = new String[]{};
        if (CommandParser.isConjunctiveMode(queryParams)) {
            String mode = askForScoringFunction();
            //documents = QueryProcesser.processConjunctiveQuery(queryParams[0], k, scoringFunction);
            showDocumentsResults(documents);
            return true;
        }

        if (CommandParser.isDisjunctiveMode(queryParams)) {
            String mode = askForScoringFunction();
            //documents = QueryProcesser.processDisjunctiveQuery(queryParams[0], k, scoringFunction);
            showDocumentsResults(documents);
            return true;
        }

        System.out.println("The query you entered is in invalid format.");
        return false;
    }

    private static void showDocumentsResults(String[] documents) {
        if (documents.length == 0) {
            System.out.println("No documents found.");
            return;
        }

        System.out.println("Documents found:");
        for (String document : documents) {
            System.out.println(document);
        }
    }

    private static String askForScoringFunction() {
        System.out.println("Which scoring function would you like to apply?\nType tfidf or bm25");
        String response = scanner.nextLine();

        if (CommandParser.isTfIdfScoring(response)) {
            return "tfidf";
        }

        if (CommandParser.isBM25Scoring(response)) {
            return "bm25";
        }

        System.out.println("Invalid scoring function. Please try again.");
        return askForScoringFunction();
    }
}

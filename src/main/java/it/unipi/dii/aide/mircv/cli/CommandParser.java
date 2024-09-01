package it.unipi.dii.aide.mircv.cli;

public class CommandParser {

    public static boolean isHelpCommand(String[] queryParams) {
        return queryParams[0].equals("help");
    }

    public static boolean isBreakCommand(String[] queryParams) {
        if(queryParams[0].equals("break")){
            System.out.println("Bye.. Hope you have found everything you were looking for :)");
            return true;
        }

        return false;
    }

    public static boolean isValidQuery(String query) {
        return query != null && !query.isEmpty();
    }

    public static String[] parseQuery(String query) {
        return query.split("-");
    }

    public static boolean isConjunctiveMode(String[] queryParams) {
        return queryParams[1].equals("c") ;
    }

    public static boolean isDisjunctiveMode(String[] queryParams) {
        return queryParams[1].equals("d");
    }

    public static boolean isTfIdfScoring(String response) {
        return response.equals("tfidf");
    }

    public static boolean isBM25Scoring(String response) {
        return response.equals("bm25");
    }
}

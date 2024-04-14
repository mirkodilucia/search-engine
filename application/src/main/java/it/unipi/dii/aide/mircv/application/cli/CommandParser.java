package it.unipi.dii.aide.mircv.application.cli;

public class CommandParser {

    public static boolean isHelpCommand(String[] queryParams) {
        if(queryParams[0].equals("help")){
            System.out.println("""
                    Please insert a query specifying your preferred mode:
                                -c for conjunctive mode or -d for disjunctive mode. Here's an example:s
                                This is a query example -c s
                                Insert -h for help or -b to terminate the service""");
            return true;
        }
        return false;
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
        return queryParams[1].equals("c");
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

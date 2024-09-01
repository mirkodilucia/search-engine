package it.unipi.dii.aide.mircv.cli;

public class CommandParser {

    public static boolean isHelpCommand(String[] queryParams) {
        return queryParams[0].equals("help");
    }

    /**
     * Check if the user wants to break the service
     * @param queryParams
     * @return
     */
    public static boolean isBreakCommand(String[] queryParams) {
        if(queryParams[0].equals("break")){
            System.out.println("Bye.. Hope you have found everything you were looking for :)");
            return true;
        }

        return false;
    }

    /**
     * Check if the query is valid
     * @param query
     * @return
     */
    public static boolean isValidQuery(String query) {
        return query != null && !query.isEmpty();
    }

    /**
     * Parse the query
     * @param query
     * @return
     */
    public static String[] parseQuery(String query) {
        return query.split("-");
    }

    /**
     * Check if the user wants to use conjunctive mode
     * @param queryParams
     * @return boolean
     */
    public static boolean isConjunctiveMode(String[] queryParams) {
        return queryParams[1].equals("c") ;
    }

    /**
     * Check if the user wants to use disjunctive mode
     * @param queryParams
     * @return boolean
     */
    public static boolean isDisjunctiveMode(String[] queryParams) {
        return queryParams[1].equals("d");
    }

    /**
     * Check if the user wants to use the max score function
     * @param response The user response
     * @return boolean
     */
    public static boolean isTfIdfScoring(String response) {
        return response.equals("tfidf");
    }

    /**
     * Check if the user wants to use the bm25 score function
     * @param response The user response
     * @return boolean
     */
    public static boolean isBM25Scoring(String response) {
        return response.equals("bm25");
    }
}

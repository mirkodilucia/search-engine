package it.unipi.dii.aide.mircv.preprocess;

public class PreProcessor {

    private static final String URL_MATCHER = "[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";
    private static final String HTML_TAGS_MATCHER = "<[^>]+>";
    private static final String NON_DIGIT_AND_PUNCTUATION_MATCHER = "[^a-zA-Z ]";
    private static final String CONSECUTIVE_LETTERS_MATCHER = "(.)\\1{2,}";

    private static final String MULTIPLE_SPACE_MATCHER = " +";
    private static final String CAMEL_CASE_MATCHER = "(?<=[a-z])(?=[A-Z])";

    private static final String REPLACEMENT = " ";

    /**
     * Clean the URL from the input
     * @param input the input string
     * @return the cleaned string
     */
    public String cleanUrl(String input) {
        return cleanByRegex(input, URL_MATCHER, REPLACEMENT);
    }

    /**
     * Clean the HTML tags from the input
     * @param input the input string
     * @return the cleaned string
     */
    public String cleanHtmlTags(String input) {
        return cleanByRegex(input, HTML_TAGS_MATCHER, REPLACEMENT);
    }

    /**
     * Clean the non digit and punctuation from the input
     * @param input the input string
     * @return the cleaned string
     */
    public String cleanNonDigit(String input) {
        return cleanByRegex(input, NON_DIGIT_AND_PUNCTUATION_MATCHER, REPLACEMENT);
    }

    /**
     * Clean the consecutive letters from the input
     * @param input the input string
     * @return the cleaned string
     */
    public String cleanConsecutiveLetters(String input) {
        return cleanByRegex(input, CONSECUTIVE_LETTERS_MATCHER, "$1$1");
    }

    /**
     * Clean the multiple spaces from the input
     * @param input the input string
     * @return the cleaned string
     */
    public String cleanMultipleSpaces(String input) {
        return cleanByRegex(input, MULTIPLE_SPACE_MATCHER, " ");
    }

    /**
     * Add space to camel case from the input
     * @param input the input string
     * @return the cleaned string
     */
    public String addSpaceToCamelCase(String input) {
        return cleanByRegex(input, CAMEL_CASE_MATCHER, REPLACEMENT);
    }

    /**
     * Clean the input string by the regex pattern
     * @param input the input string
     * @param regexPattern the regex pattern
     * @param replacement the replacement string
     * @return the cleaned string
     */
    public String cleanByRegex(String input, String regexPattern, String replacement) {
        return input.replaceAll(regexPattern, replacement);
    }

    /**
     * Clean the input string
     * @param input the input string
     * @return the cleaned string
     */
    public String cleanText(String input) {
        String result = input;

        result = cleanUrl(result);
        result = cleanHtmlTags(result);
        result = cleanNonDigit(result);
        result = cleanConsecutiveLetters(result);
        result = cleanMultipleSpaces(result);
        result = addSpaceToCamelCase(result);

        return result.trim();
    }

    private PreProcessor() {}

    private static PreProcessor INSTANCE;

    public static PreProcessor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PreProcessor();
        }
        return INSTANCE;
    }

}
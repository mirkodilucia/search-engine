package it.unipi.dii.aide.mircv.preprocessor;

public class PreProcessor {


    private static final String URL_MATCHER = "[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";
    private static final String HTML_TAGS_MATCHER = "<[^>]+>";
    private static final String NON_DIGIT_AND_PUNCTUATION_MATCHER = "[^a-zA-Z ]";
    private static final String CONSECUTIVE_LETTERS_MATCHER = "(.)\\1{2,}";

    private static final String MULTIPLE_SPACE_MATCHER = " +";
    private static final String CAMEL_CASE_MATCHER = "(?<=[a-z])(?=[A-Z])";

    private static final String REPLACEMENT = " ";

    public String cleanUrl(String input) {
        return cleanByRegex(input, URL_MATCHER, REPLACEMENT).trim();
    }

    public String cleanHtmlTags(String input) {
        return cleanByRegex(input, HTML_TAGS_MATCHER, REPLACEMENT).trim();
    }

    public String cleanNonDigit(String input) {
        return cleanByRegex(input, NON_DIGIT_AND_PUNCTUATION_MATCHER, REPLACEMENT).trim();
    }

    public String cleanConsecutiveLetters(String input) {
        return input.replaceAll(CONSECUTIVE_LETTERS_MATCHER, "$1$1").trim();
    }

    public String cleanMultipleSpaces(String input) {
        return input.replaceAll(MULTIPLE_SPACE_MATCHER, " ").trim();
    }

    public String addSpaceToCamelCase(String input) {
        return input.replaceAll(CAMEL_CASE_MATCHER, REPLACEMENT).trim();
    }

    public String cleanByRegex(String input, String regexPattern, String replacement) {
        return input.replaceAll(regexPattern, replacement);
    }

    public String cleanText(String input) {
        String result = input;

        result = cleanUrl(result);
        result = cleanHtmlTags(result);
        result = cleanNonDigit(result);
        result = cleanConsecutiveLetters(result);
        result = cleanMultipleSpaces(result);
        result = addSpaceToCamelCase(result);


        return result;
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

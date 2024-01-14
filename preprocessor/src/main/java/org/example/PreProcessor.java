package org.example;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PreProcessor {

    private static final String URL_MATCHER = "[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";
    private static final String HTML_TAGS_MATCHER = "<[^>]+>";
    private static final String NON_DIGIT_MATCHER = "[^a-zA-Z ]";
    private static final String MULTIPLE_SPACE_MATCHER = " +";
    private static final String CONSECUTIVE_LETTERS_MATCHER = "(.)\\1{2,}";

    private static final String CAMEL_CASE_MATCHER = "(?<=[a-z])(?=[A-Z])";

    private static final String REPLACEMENT = "\s";

    public String cleanUrl(StringBuilder result) {
        return "";
    }

    public String cleanByRegex(String input, String regexPattern) {
        StringBuilder result = new StringBuilder();

        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            matcher.appendReplacement(result, REPLACEMENT);
        }

        return result.toString();
    }

    public String cleanText(String input) {
        StringBuilder result = new StringBuilder();

        // Input string
        String regexPattern = "[A-Z]{3}\\d{3}";

        // Replacement string
        String replacement = "\s";

        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            matcher.appendReplacement(result, replacement);
        }

        return result.toString();
    }

}

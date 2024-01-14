package org.example;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PreProcessor {


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

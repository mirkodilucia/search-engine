package it.unipi.dii.aide.mircv.preprocess;

import ca.rmen.porterstemmer.PorterStemmer;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.utils.FileHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class Stemmer {

    private static final String CAMEL_CASE_MATCHER = "(?<=[a-z])(?=[A-Z])";
    private static final String SPACE = " ";

    private static final int THRESHOLD = 64;
    private static Stemmer INSTANCE;

    private static HashMap<String, Integer> stopwords = new HashMap<>();
    private final Config configuration;

    private Stemmer(Config configuration) {
        this.configuration = configuration;
        loadStopwords();
    }

    public String[] removeStopwords(String[] words) {
        ArrayList<String> meaningfulToken = new ArrayList<>();

        for (String word : words) {
            if (stopwords.containsKey(word) || word.length() > THRESHOLD) {
                continue;
            }

            meaningfulToken.add(word);
        }

        return meaningfulToken.toArray(new String[0]);
    }

    public String[] getStems(String[] words) {
        PorterStemmer stemmer = new PorterStemmer();

        for (int i=0; i<words.length; i++) {
            words[i] = stemmer.stemWord(words[i]);
        }

        return words;
    }

    public String[] tokenize(String text) {
        ArrayList<String> tokens = new ArrayList<>();

        String[] words = text.split(SPACE);
        for (String token : words) {
            String[] subtokens = token.split(CAMEL_CASE_MATCHER);

            for (String subtoken : subtokens) {
                //truncate subtoken to THRESHOLD characters
                subtoken = subtoken.substring(0, Math.min(subtoken.length(), THRESHOLD));
                //return token in lower case
                tokens.add(subtoken.toLowerCase(Locale.ROOT));
            }
        }

        return tokens.toArray(new String[0]);
    }

    private void loadStopwords() {
        if (!stopwords.isEmpty()) {
            throw new IllegalStateException("Stopwords already loaded");
        }

        try {
            stopwords = FileHandler.readStopwordLines(configuration.getPreprocessConfig().getStopwordsPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stemmer with(Config configuration) {

        if (INSTANCE == null) {
            INSTANCE = new Stemmer(configuration);
        }

        return INSTANCE;
    }

    public String processDocument(String input) {
        String[] tokens = tokenize(input);
        String[] meaningfulTokens = removeStopwords(tokens);
        String[] stemmedTokens = getStems(meaningfulTokens);

        StringBuilder sb = new StringBuilder();
        for (String token : stemmedTokens) {
            sb.append(token).append(SPACE);
        }

        return sb.toString().trim();
    }

}

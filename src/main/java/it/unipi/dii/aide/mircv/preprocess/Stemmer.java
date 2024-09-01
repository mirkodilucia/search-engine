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

    /**
     * Remove stopwords from the input words
     * @param words the input words
     * @return the words without stopwords
     */
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

    /**
     * Get the stems of the input words
     * @param words the input words
     * @return the stems of the input words
     */
    public String[] getStems(String[] words) {
        PorterStemmer stemmer = new PorterStemmer();

        for (int i=0; i<words.length; i++) {
            words[i] = stemmer.stemWord(words[i]);
        }

        return words;
    }

    /**
     * Tokenize the input text into words
     * @param text the input text
     * @return the tokens of the input text
     */
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

    /**
     * Load the stopwords from the file
     */
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

    /**
     * Get the instance of the Stemmer
     * @param configuration the configuration
     * @return the instance of the Stemmer
     */
    public static Stemmer with(Config configuration) {

        if (INSTANCE == null) {
            INSTANCE = new Stemmer(configuration);
        }

        return INSTANCE;
    }

    /**
     * Process the input document
     * @param input the input document
     * @return the processed document
     */
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

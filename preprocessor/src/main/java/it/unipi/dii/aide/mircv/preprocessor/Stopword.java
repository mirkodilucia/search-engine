package it.unipi.dii.aide.mircv.preprocessor;

import ca.rmen.porterstemmer.PorterStemmer;
import it.unipi.dii.aide.mircv.utils.FileUtils;

import java.util.ArrayList;
import java.util.Locale;

public class Stopword {

    private static final String CAMEL_CASE_MATCHER = "(?<=[a-z])(?=[A-Z])";

    private static final int THRESHOLD = 64;
    private static Stopword INSTANCE;

    private static final String SPACE = " ";

    private static final PorterStemmer stemmer = new PorterStemmer();

    private static final ArrayList<String> stopwords = new ArrayList<>();

    private Stopword() {
        loadStopwords();
    }

    public String[] removeStopwords(String[] words) {
        ArrayList<String> meaningfulToken = new ArrayList<>();

        for (String word : words) {
            if (stopwords.contains(word) || word.length() > THRESHOLD) {
                continue;
            }

            meaningfulToken.add(word);
        }

        return meaningfulToken.toArray(new String[0]);
    }

    public static String[] getStems(String[] words) {
        ArrayList<String> stemmedTokens = new ArrayList<>();

        for (String word : words) {
            stemmedTokens.add(stemmer.stemWord(word));
        }

        return stemmedTokens.toArray(new String[0]);
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
            stopwords.addAll(FileUtils.readStopwordLines());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stopword getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Stopword();
        }

        return INSTANCE;
    }
}

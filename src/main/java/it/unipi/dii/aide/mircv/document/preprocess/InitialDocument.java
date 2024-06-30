package it.unipi.dii.aide.mircv.document.preprocess;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.preprocess.Stemmer;
import it.unipi.dii.aide.mircv.utils.FileHandler;

import java.io.IOException;

public class InitialDocument {

    private final static String RAW_COLLECTION_PATH = "data/raw_collection";

    private final String docId;
    private final Stemmer stemmer;
    private final Config config;

    String plainText;

    String[] tokens;
    String[] relevantTokens;
    String[] stems;

    public static InitialDocument load(Config config, int documentId) {
        String docId = "doc" + documentId;
        String content = readFileString(docId);
        if (content == null) {
            return null;
        }
        return new InitialDocument(config, docId, content);
    }

    public InitialDocument(Config config, String docId, String content) {
        this.config = config;
        stemmer = Stemmer.with(config);

        this.docId = docId;
        this.plainText = content;
    }

    private void cleanText() {
        plainText = plainText.replaceAll("[^a-zA-Z0-9]", " ");
    }

    private void tokenize() {
        tokens = plainText.split("\\s+");
    }

    public FinalDocument process() {
        this.cleanText();
        this.tokenize();

        /** TODO: Implement the following methods **/
        if (config.getPreprocessConfig().isStemmerEnabled()) {
         removeStopwords();
         stem();
        }

        return new FinalDocument(docId, tokens);
    }

    public void removeStopwords() {
        this.relevantTokens = stemmer.removeStopwords(this.tokens);
    }

    public void stem() {
        this.stems = stemmer.getStems(this.relevantTokens);
    }


    public void writeFileString() {
        try {
            this.process();

            String fileData = this.docId + ";" + String.join(",", this.tokens);
            FileHandler.writeStringToFile(RAW_COLLECTION_PATH + "/" + docId, fileData);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readFileString(String docId) {
        try {
            if (!FileHandler.fileExists(RAW_COLLECTION_PATH + "/" + docId)) {
                return null;
            }

            return FileHandler.readStringFromFile(RAW_COLLECTION_PATH + "/" + docId);
        }catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}

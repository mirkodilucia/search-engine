package it.unipi.dii.aide.mircv.document.preprocess;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.preprocess.PreProcessor;
import it.unipi.dii.aide.mircv.preprocess.Stemmer;
import it.unipi.dii.aide.mircv.utils.FileHandler;

import java.io.IOException;

public class InitialDocument {

    public final static String RAW_COLLECTION_PATH = "data/raw_collection";

    private final String docId;
    private final Stemmer stemmer;
    private final PreProcessor preProcessor = PreProcessor.getInstance();
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


    public FinalDocument process() {
        plainText = preProcessor.cleanText(plainText);
        tokens = stemmer.tokenize(plainText);

        if (config.getPreprocessConfig().isStemmerEnabled()) {
            tokens = removeStopwords(tokens);
            stem(tokens);
        }

        return new FinalDocument(docId, tokens);
    }

    public String[] removeStopwords(String[] tokens) {
        return stemmer.removeStopwords(tokens);
    }

    public String[] stem(String[] tokens) {
        return stemmer.getStems(tokens);
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

package it.unipi.dii.aide.mircv.document.preprocess;

import it.unipi.dii.aide.mircv.utils.FileHandler;

import java.io.IOException;
import java.util.List;

public class InitialDocument {

    private final static String RAW_COLLECTION_PATH = "data/raw_collection";

    private String docId;

    String plainText;

    String[] tokens;

    public static InitialDocument load(int documentId) {
        String docId = "doc" + documentId;
        String content = readFileString(docId);
        if (content == null) {
            return null;
        }
        return new InitialDocument(docId, content);
    }

    public InitialDocument(String docId, String content) {
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

        /** TODO: Implement the following methods
         if(config.preprocessConfig.isStemmerEnabled()) {
         removeStopwords();
         stem();
         }
         */

        return new FinalDocument(docId, tokens);
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

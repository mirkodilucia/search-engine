package it.unipi.dii.aide.mircv.application.data;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.preprocessor.PreProcessor;
import it.unipi.dii.aide.mircv.application.utils.FileUtils;
import it.unipi.dii.aide.mircv.application.preprocessor.Stemmer;

import java.util.ArrayList;

public class InitialDocument {

    private final Stemmer stemmer;
    private final Config config;
    protected String docId;
    private String plainText;
    private String[] tokens;
    private String[] relevantTokens;
    private String[] stems;
    private static String PATH_TO_COLLECTION;

    public void setupTextDocument() {
        PATH_TO_COLLECTION = config.getCollectionConfig().getRawCollectionPath();
    }

    public InitialDocument(Config config, String docId, String plainText) {
        this.config = config;
        stemmer = Stemmer.with(config);

        this.docId = docId;
        this.plainText = plainText;
    }

    public void cleanText() {
        this.plainText = PreProcessor.getInstance().cleanText(this.plainText);
    }

    public void tokenize() {
        this.tokens = stemmer.tokenize(this.plainText);
    }

    public void removeStopwords() {
        this.relevantTokens = stemmer.removeStopwords(this.tokens);
    }

    public void stem() {
        this.stems = stemmer.getStems(this.relevantTokens);
    }

    public void writeFileString() {
        String fileData = this.docId + ";" + String.join(",", this.stems);
        FileUtils.writeFile(PATH_TO_COLLECTION+ "/" + docId, fileData);
    }

    public void setTokens(ArrayList<String> tokens1) {
        this.tokens = tokens1.toArray(new String[0]);
    }

    /**
     * Perform the preprocessing of a InitialDocument, transforming it in a document formed by
     * its PID and the list of its tokens
     * @return the processed document
     */
    public FinalDocument processDocument() {
        this.cleanText();
        this.tokenize();

        if(config.preprocessConfig.isStemmerEnabled()) {
            removeStopwords();
            stem();
        }

        // Return the processed document
        return new FinalDocument(docId, tokens);
    }
}

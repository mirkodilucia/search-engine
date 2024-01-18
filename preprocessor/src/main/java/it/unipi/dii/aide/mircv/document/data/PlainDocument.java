package it.unipi.dii.aide.mircv.document.data;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.preprocessor.PreProcessor;
import it.unipi.dii.aide.mircv.preprocessor.Stemmer;

public class PlainDocument {

    private final Stemmer stemmer;
    protected String docId;
    private String plainText;
    private String[] tokens;
    private String[] relevantTokens;
    private String[] stems;

    protected String getDocId() {
        return docId;
    }

    public PlainDocument(Config config, String docId, String plainText) {
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
}

package it.unipi.dii.aide.mircv.document.data;

import it.unipi.dii.aide.mircv.preprocessor.PreProcessor;
import it.unipi.dii.aide.mircv.preprocessor.Stemmer;

public class PlainDocument {

    protected String docId;

    protected String getDocId() {
        return docId;
    }

    private String plainText;
    private String[] tokens;
    private String[] relevantTokens;

    private String[] stems;

    public PlainDocument(String docId, String plainText) {
        this.docId = docId;
        this.plainText = plainText;
    }

    public void cleanText() {
        this.plainText = PreProcessor.getInstance().cleanText(this.plainText);
    }

    public void tokenize() {
        this.tokens = Stemmer.getInstance().tokenize(this.plainText);
    }

    public void removeStopwords() {
        this.relevantTokens = Stemmer.getInstance().removeStopwords(this.tokens);
    }

    public String[] stem() {
        this.stems = Stemmer.getInstance().getStems(this.relevantTokens);
        return this.stems;
    }
}

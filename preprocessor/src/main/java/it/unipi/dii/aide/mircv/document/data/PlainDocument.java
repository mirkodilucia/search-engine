package it.unipi.dii.aide.mircv.document.data;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.preprocessor.PreProcessor;
import it.unipi.dii.aide.mircv.preprocessor.Stemmer;
import it.unipi.dii.aide.mircv.utils.FileUtils;

public class PlainDocument {

    private final Stemmer stemmer;
    private final Config config;
    protected String docId;
    private String plainText;
    private String[] tokens;
    private String[] relevantTokens;
    private String[] stems;

    public PlainDocument(Config config, String docId, String plainText) {
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
        FileUtils.writeFile(config.rawCollectionPath + "/" + docId, fileData);
    }
}

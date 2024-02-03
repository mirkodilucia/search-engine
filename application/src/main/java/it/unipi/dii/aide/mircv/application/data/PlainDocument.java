package it.unipi.dii.aide.mircv.application.data;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.preprocessor.PreProcessor;
import it.unipi.dii.aide.mircv.application.utils.FileUtils;
import it.unipi.dii.aide.mircv.application.preprocessor.Stemmer;

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
        FileUtils.writeFile(config.getRawCollectionPath() + "/" + docId, fileData);
    }
}

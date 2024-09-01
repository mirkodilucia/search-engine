package it.unipi.dii.aide.mircv.config.model;

public class PartialResultsConfig {
    private final String partialVocabularyDir; //Directory
    private final String frequencyDir; //Directory
    private final String docIdDir; //Directory

    public PartialResultsConfig(String partialVocabularyDir, String frequencyDir, String docIdDir) {
        this.partialVocabularyDir = partialVocabularyDir;
        this.frequencyDir = frequencyDir;
        this.docIdDir = docIdDir;
    }

    public String getPartialVocabularyDir() {
        return partialVocabularyDir;
    }


    public String getFrequencyDir() {
        return frequencyDir;
    }

    public String getDocIdDir() {
        return docIdDir;
    }
}

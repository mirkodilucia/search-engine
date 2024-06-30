package it.unipi.dii.aide.mircv.config;

//Related to the paths of the vocabulary file
public class VocabularyConfig {

    private final String vocabularyFilePath;  //File

    public VocabularyConfig(String vocabularyFile) {
        this.vocabularyFilePath = vocabularyFile;
    }

    public String getVocabularyFile() {
        return vocabularyFilePath;
    }

    public String getVocabularyPath() {
        return vocabularyFilePath;
    }
}

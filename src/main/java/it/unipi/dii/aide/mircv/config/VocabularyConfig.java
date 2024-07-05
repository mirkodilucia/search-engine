package it.unipi.dii.aide.mircv.config;

//Related to the paths of the vocabulary file
public class VocabularyConfig {

    private final String vocabularyFilePath;  //File

    private final String documentIndexStatePath; //File

    public VocabularyConfig(
            String vocabularyFile,
            String documentIndexStatePath
    ) {
        this.vocabularyFilePath = vocabularyFile;
        this.documentIndexStatePath = documentIndexStatePath;
    }

    public String getVocabularyPath() {
        return vocabularyFilePath;
    }

    public String getCollectionStatisticsPath() {
        return documentIndexStatePath;
    }

}

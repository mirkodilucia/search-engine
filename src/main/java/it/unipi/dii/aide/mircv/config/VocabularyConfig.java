package it.unipi.dii.aide.mircv.config;

//Related to the paths of the vocabulary file
public class VocabularyConfig {

    private final String vocabularyFile;  //File
    private final String frequencyFileName; //File
    private final String docIdFileName; //File
    private final String vocabularyPath;

    public VocabularyConfig(String vocabularyFile, String frequencyFileNAme, String docIdFileName, String vocabularyPath) {
        this.vocabularyFile = vocabularyFile;
        this.frequencyFileName = frequencyFileNAme;
        this.docIdFileName = docIdFileName;
        this.vocabularyPath = vocabularyPath;
    }

    public String getVocabularyFile() {
        return vocabularyFile;
    }


    public String getFrequencyFileName() {
        return frequencyFileName;
    }

    public String getDocIdFileName() {
        return docIdFileName;
    }

    public String getVocabularyPath() {
        return vocabularyPath;
    }
}

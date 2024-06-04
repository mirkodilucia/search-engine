package it.unipi.dii.aide.mircv.application.config;

import it.unipi.dii.aide.mircv.application.utils.FileUtils;

//Related to the paths of the vocabulary file
public class VocabularyConfig {

    private final String vocabularyFile;  //File
    private final String frequencyFileName; //File
    private final String docIdFileName; //File
    private final String vocabularyPath;

    public VocabularyConfig(String vocabularyPath, String frequencyFileNAme, String docIdFileName) {
        this.vocabularyFile = vocabularyPath;

        this.frequencyFileName = frequencyFileNAme;
        this.docIdFileName = docIdFileName;
    }

    public String getVocabularyFile() {
        return vocabularyFile;
    }


    public void cleanUp() {

        FileUtils.removeFile(vocabularyFile);
    }

    public String getFrequencyFileName() {
        return frequencyFileName;
    }

    public String getDocIdFileName() {
        return docIdFileName;
    }
}

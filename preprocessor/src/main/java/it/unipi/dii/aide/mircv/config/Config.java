package it.unipi.dii.aide.mircv.config;

import java.io.Serializable;

public class Config implements Serializable {

    private Boolean removeStopwords;
    private String datasetPath;
    private String rawCollectionPath;
    private String compressedCollectionPath;
    private String stopwordsPath;
    private String documentIndexPath;
    private String vocabularyPath;
    private String invertedIndexFreqs;
    private String invertedIndexDocs;
    private String partialVocabularyDir;
    private String frequencyFileName;
    private String docidsFileName;
    private String vocabularyFileName;
    private String frequencyDir;
    private String docidsDir;
    private String collectionStatisticsPath;
    private String blockDescriptorsPath;
    private String flagsFilePath;
    private String testDir;

    private String debugDir;


    public String getPathToVocabulary() {
        return vocabularyPath;
    }

    public String getPathToInvertedIndexDocs() {
        return invertedIndexDocs;
    }

    public String getPathToInvertedIndexFreqs() {
        return invertedIndexFreqs;
    }

    public String getPathToBlockDescriptors() {
        return blockDescriptorsPath;
    }

    private String getPathToPartialIndexesDocs() {
        return this.getDocumentIdFolder() + this.getDocumentIdFileName();
    }

    private String getPathToPartialIndexesFreqs() {
        return this.getFrequencyFolder() + this.getFrequencyFileName();
    }

    private String getPathToPartialVocabularies() {
        return this.getPartialVocabularyFolder() + this.getVocabularyFileName();
    }

    public String getBlockDescriptorsPath() {
        return blockDescriptorsPath;
    }

    public String getDocumentIdFolder() {
        return docidsDir;
    }

    public String getDocumentIdFileName() {
        return docidsFileName;
    }

    public String getFrequencyFolder() {
        return frequencyDir;
    }

    public String getFrequencyFileName() {
        return frequencyFileName;
    }

    public String getPartialVocabularyFolder() {
        return partialVocabularyDir;
    }

    public String getVocabularyFileName() {
        return vocabularyFileName;
    }

    public String getPartialVocabularyPath(int i) {
        return this.getPartialVocabularyFolder() + "vocabulary_" + i;
    }
}
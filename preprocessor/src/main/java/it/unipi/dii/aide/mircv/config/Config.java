package it.unipi.dii.aide.mircv.config;

import java.io.Serializable;

public class Config implements Serializable {

    public Boolean removeStopwords;
    public String datasetPath;
    public String rawCollectionPath;
    public String compressedCollectionPath;
    public String stopwordsPath;
    public String documentIndexPath;
    public String vocabularyPath;
    public String invertedIndexFreqs;
    public String invertedIndexDocs;
    public String partialVocabularyDir;
    public String frequencyFileName;
    public String docidsFileName;
    public String vocabularyFileName;
    public String frequencyDir;
    public String docidsDir;
    public String collectionStatisticsPath;
    public String blockDescriptorsPath;
    public String flagsFilePath;
    public String testDir;

    public String debugDir;
}
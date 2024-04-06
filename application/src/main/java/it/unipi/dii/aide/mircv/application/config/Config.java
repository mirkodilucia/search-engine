package it.unipi.dii.aide.mircv.application.config;

import java.io.Serializable;

public class Config implements Serializable {

    private boolean removeStopwords;
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
    private String docIdsFileName;
    private String vocabularyFileName;
    private String frequencyDir;
    private String docIdsDir;
    private String collectionStatisticsPath;
    private String blockDescriptorsPath;
    private String flagsFilePath;
    private String testDir;

    private String debugDir;
    private boolean compressionEnabled;


    public String getPathToVocabulary() {
        return vocabularyPath;
    }

    public String getPathToInvertedIndexDocs() {
        return this.invertedIndexDocs;
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
        return docIdsDir;
    }

    public String getDocumentIdFileName() {
        return docIdsFileName;
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

    public boolean isCompressionEnabled() {
        return this.compressionEnabled;
    }

    public String getDocumentIndexPath() {
        return this.documentIndexPath;
    }

    public String getRawCollectionPath() {
        return this.rawCollectionPath;
    }

    public String getStopwordsPath() {
        return this.stopwordsPath;
    }

    public String getCollectionStatisticsPath() {
        return this.collectionStatisticsPath;
    }

    public String getPartialIndexDocsPath(int i) {
        return this.getPathToPartialIndexesDocs() + "_" + i;
    }

    public String getPartialIndexFreqsPath(int i) {
        return this.getPathToPartialIndexesFreqs() + "_" + i;
    }

    public void setStopwordsPath(String stopwordsPath) {
        this.stopwordsPath = stopwordsPath;
    }

    public void setRemoveStopword(boolean removeStopwords) {
        this.removeStopwords = removeStopwords;
    }

    public boolean getRemoveStopwords() {
        return this.removeStopwords;
    }

    public String getDebugFolder() {
        return this.debugDir;
    }

    public String getPathToCompressedCollection() {
        return this.compressedCollectionPath;
    }

    public void setPathToInvertedIndexDocs(String invertedIndexDocs) {
        this.invertedIndexDocs = invertedIndexDocs;
    }

    public void setPathToInvertedIndexFreq(String invertedIndexFreqs) {
        this.invertedIndexFreqs = invertedIndexFreqs;
    }

    public void setPathToBlockDescriptors(String blockDescriptorsPath) {
        this.blockDescriptorsPath = blockDescriptorsPath;
    }

    public void setPartialIndexesPath(String partialVocabularyDir) {
        this.partialVocabularyDir = partialVocabularyDir;
    }

    public void setPathToVocabulary(String vocabularyPath) {
        this.vocabularyPath = vocabularyPath;
    }

    public void setCollectionStatisticsPath(String collectionStatisticsPath) {
        this.collectionStatisticsPath = collectionStatisticsPath;
    }

    public boolean isStemStopRemovalEnabled() {
        return this.removeStopwords;
    }

    public boolean isDebugEnabled() {
        return this.debugDir != null;
    }
}
package it.unipi.dii.aide.mircv.application.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "it/unipi/dii/aide/mircv/application/config")
public class Config {
    private static String rawCollectionPath;
    private static String compressedCollectionPath;
    private static String stopwordsPath;
    private static String documentIndexPath;
    private static String vocabularyPath;
    private static String invertedIndexFreqs;
    private static String invertedIndexDocs;
    private static String partialVocabularyDir;
    private static String frequencyFileName;
    private static String docidsFileName;
    private static String vocabularyFileName;
    private static String frequencyDir;
    private static String docidsDir;
    private static String collectionStatisticsPath;
    private static String blockDescriptorsPath;
    private static String flagsFilePath;

    public static String getRawCollectionPath() {
        return rawCollectionPath;
    }

    @XmlElement(name = "rawCollectionPath")
    public void setRawCollectionPath(String rawCollectionPath) {
        Config.rawCollectionPath = rawCollectionPath;
    }

    public static String getCompressedCollectionPath() {
        return compressedCollectionPath;
    }

    @XmlElement(name = "compressedCollectionPath")
    public void setCompressedCollectionPath(String compressedCollectionPath) {
        Config.compressedCollectionPath = compressedCollectionPath;
    }

    public static String getStopwordsPath() {
        return stopwordsPath;
    }

    @XmlElement(name = "stopwordsPath")
    public void setStopwordsPath(String stopwordsPath) {
        Config.stopwordsPath = stopwordsPath;
    }

    public static String getDocumentIndexPath() {
        return documentIndexPath;
    }

    @XmlElement(name = "documentIndexPath")
    public void setDocumentIndexPath(String documentIndexPath) {
        Config.documentIndexPath = documentIndexPath;
    }

    public static String getVocabularyPath() {
        return vocabularyPath;
    }

    @XmlElement(name = "vocabularyPath")
    public void setVocabularyPath(String vocabularyPath) {
        Config.vocabularyPath = vocabularyPath;
    }

    public static String getInvertedIndexFreqs() {
        return invertedIndexFreqs;
    }

    @XmlElement(name = "invertedIndexFreqs")
    public void setInvertedIndexFreqs(String invertedIndexFreqs) {
        Config.invertedIndexFreqs = invertedIndexFreqs;
    }

    public static String getInvertedIndexDocs() {
        return invertedIndexDocs;
    }

    @XmlElement(name = "invertedIndexDocs")
    public void setInvertedIndexDocs(String invertedIndexDocs) {
        Config.invertedIndexDocs = invertedIndexDocs;
    }

    public static String getPartialVocabularyDir() {
        return partialVocabularyDir;
    }

    @XmlElement(name = "partialVocabularyDir")
    public void setPartialVocabularyDir(String partialVocabularyDir) {
        Config.partialVocabularyDir = partialVocabularyDir;
    }

    public static String getFrequencyFileName() {
        return frequencyFileName;
    }

    @XmlElement(name = "frequencyFileName")
    public void setFrequencyFileName(String frequencyFileName) {
        Config.frequencyFileName = frequencyFileName;
    }

    public static String getDocidsFileName() {
        return docidsFileName;
    }

    @XmlElement(name = "docidsFileName")
    public void setDocidsFileName(String docidsFileName) {
        Config.docidsFileName = docidsFileName;
    }

    public static String getVocabularyFileName() {
        return vocabularyFileName;
    }

    @XmlElement(name = "vocabularyFileName")
    public void setVocabularyFileName(String vocabularyFileName) {
        Config.vocabularyFileName = vocabularyFileName;
    }

    public static String getFrequencyDir() {
        return frequencyDir;
    }

    @XmlElement(name = "frequencyDir")
    public void setFrequencyDir(String frequencyDir) {
        Config.frequencyDir = frequencyDir;
    }

    public static String getDocidsDir() {
        return docidsDir;
    }

    @XmlElement(name = "docidsDir")
    public void setDocidsDir(String docidsDir) {
        Config.docidsDir = docidsDir;
    }

    public static String getCollectionStatisticsPath() {
        return collectionStatisticsPath;
    }

    @XmlElement(name = "collectionStatisticsPath")
    public void setCollectionStatisticsPath(String collectionStatisticsPath) {
        Config.collectionStatisticsPath = collectionStatisticsPath;
    }

    public static String getBlockDescriptorsPath() {
        return blockDescriptorsPath;
    }

    @XmlElement(name = "blockDescriptorsPath")
    public void setBlockDescriptorsPath(String blockDescriptorsPath) {
        Config.blockDescriptorsPath = blockDescriptorsPath;
    }

    public static String getFlagsFilePath() {
        return flagsFilePath;
    }

    @XmlElement(name = "flagsFilePath")
    public void setFlagsFilePath(String flagsFilePath) {
        Config.flagsFilePath = flagsFilePath;
    }

    @Override
    public String toString() {
        return "Config{" +
                "rawCollectionPath='" + rawCollectionPath + '\'' +
                ", compressedCollectionPath='" + compressedCollectionPath + '\'' +
                ", stopwordsPath='" + stopwordsPath + '\'' +
                ", documentIndexPath='" + documentIndexPath + '\'' +
                ", vocabularyPath='" + vocabularyPath + '\'' +
                ", invertedIndexFreqs='" + invertedIndexFreqs + '\'' +
                ", invertedIndexDocs='" + invertedIndexDocs + '\'' +
                ", partialVocabularyDir='" + partialVocabularyDir + '\'' +
                ", frequencyFileName='" + frequencyFileName + '\'' +
                ", docidsFileName='" + docidsFileName + '\'' +
                ", vocabularyFileName='" + vocabularyFileName + '\'' +
                ", frequencyDir='" + frequencyDir + '\'' +
                ", docidsDir='" + docidsDir + '\'' +
                ", collectionStatisticsPath='" + collectionStatisticsPath + '\'' +
                ", blockDescriptorsPath='" + blockDescriptorsPath + '\'' +
                ", flagsFilePath='" + flagsFilePath + '\'' +
                '}';
    }
}
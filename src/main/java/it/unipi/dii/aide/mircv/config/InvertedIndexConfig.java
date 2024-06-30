package it.unipi.dii.aide.mircv.config;

import it.unipi.dii.aide.mircv.utils.FileHandler;

public class InvertedIndexConfig {

    private final String invertedIndexFreqs; //File
    private final String invertedIndexDocId; //File

    public InvertedIndexConfig(
            String invertedIndexFreqs,
            String invertedIndexDocs) {

        this.invertedIndexFreqs = invertedIndexFreqs;
        this.invertedIndexDocId = invertedIndexDocs;
    }

    public String getInvertedIndexFreqsFile() {
        return invertedIndexFreqs;
    }

    public String getInvertedIndexDocs() {
        return invertedIndexDocId;
    }

    public String getPartialIndexDocumentsPath(int i) {
        return invertedIndexDocId + "_" + i + ".dat";
    }

    public String getPartialIndexFrequenciesPath(int i) {
        return invertedIndexFreqs + "_" + i + ".dat";
    }

    public void cleanUp() {
        FileHandler.removeFile(invertedIndexFreqs);
        FileHandler.removeFile(invertedIndexDocId);
    }
}

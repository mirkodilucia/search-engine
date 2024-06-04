package it.unipi.dii.aide.mircv.application.config;

import it.unipi.dii.aide.mircv.application.utils.FileUtils;

public class InvertedIndexConfig {

    //private final String blockDescriptorPath;



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


    public void cleanUp() {
        FileUtils.removeFile(invertedIndexFreqs);
        FileUtils.removeFile(invertedIndexDocId);

    }
}

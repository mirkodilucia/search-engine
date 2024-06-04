package it.unipi.dii.aide.mircv.application.config;

import it.unipi.dii.aide.mircv.application.utils.FileUtils;

//Related to the paths of the directory where the inverted index is stored
public class PartialDirectoryConfig
{
    private final String partialInvertedFrequenciesPathDir; //Directory
    private final String partialInvertedIndexDocIdPathDir; //Directory
    private final String partialVocabularyDir; //Directory
    private final String frequencyDir; //Directory
    private final String docIdDir; //Directory

    public PartialDirectoryConfig(String partialInvertedFrequenciesPathDir, String partialInvertedIndexDocIdPathDir, String partialVocabularyDir, String frequencyDir, String docIdDir) {
        this.partialInvertedFrequenciesPathDir = partialInvertedFrequenciesPathDir;
        this.partialInvertedIndexDocIdPathDir = partialInvertedIndexDocIdPathDir;
        this.partialVocabularyDir = partialVocabularyDir;
        this.frequencyDir = frequencyDir;
        this.docIdDir = docIdDir;
    }

    public String getPartialInvertedFrequenciesPathDir() {
        return partialInvertedFrequenciesPathDir;
    }

    public String getPartialInvertedIndexDocIdPathDir() {
        return partialInvertedIndexDocIdPathDir;
    }



    public String getPartialVocabularyDir() {
        return partialVocabularyDir;
    }

    public void cleanUp() {
        FileUtils.deleteFolder(partialInvertedFrequenciesPathDir);
        FileUtils.deleteFolder(partialInvertedIndexDocIdPathDir);
    }

    public String getFrequencyDir() {
        return frequencyDir;
    }

    public String getDocIdDir() {
        return docIdDir;
    }
}

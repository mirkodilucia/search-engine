package it.unipi.dii.aide.mircv.application.config;

import it.unipi.dii.aide.mircv.application.utils.FileUtils;

public class InvertedIndexConfig {

    private final String blockDescriptorPath;

    private final String documentIndexPath;

    private final String invertedIndexFreqs;
    private final String invertedIndexDocs;

    private final String partialInvertedFrequenciesPath;
    private final String partialInvertedIndexDocumentsPath;

    public InvertedIndexConfig(
            String blockDescriptorPath,
            String documentIndexPath,
            String invertedIndexFreqs,
            String invertedIndexDocs,
            String partialInvertedFrequenciesPath,
            String partialInvertedIndexDocumentsPath) {

        this.documentIndexPath = documentIndexPath;
        this.invertedIndexFreqs = invertedIndexFreqs;
        this.invertedIndexDocs = invertedIndexDocs;
        this.partialInvertedFrequenciesPath = partialInvertedFrequenciesPath;
        this.partialInvertedIndexDocumentsPath = partialInvertedIndexDocumentsPath;
        this.blockDescriptorPath = blockDescriptorPath;
    }

    public String getBlockDescriptorFile() {
        return blockDescriptorPath;
    }

    public String getDocumentIndexFile() {
        return documentIndexPath;
    }

    public String getInvertedIndexFreqsFile() {
        return invertedIndexFreqs;
    }

    public String getInvertedIndexDocs() {
        return invertedIndexDocs;
    }

    public String getPartialIndexDocumentsPath(int i) {
        return this.partialInvertedIndexDocumentsPath + "/inverted_index_docs_" + i;
    }

    public String getPartialIndexFrequenciessPath(int i) {
        return this.partialInvertedFrequenciesPath + "/inverted_index_freqs_" + i;
    }

    public String getInvertedIndexFreqsDir() {
        return this.partialInvertedFrequenciesPath;
    }

    public String getDocumentIndexDir() {
        return this.documentIndexPath;
    }

    public void cleanUp() {
        FileUtils.removeFile(documentIndexPath);
        FileUtils.removeFile(invertedIndexFreqs);
        FileUtils.removeFile(invertedIndexDocs);
        FileUtils.removeFile(partialInvertedFrequenciesPath);
        FileUtils.removeFile(partialInvertedIndexDocumentsPath);
        FileUtils.removeFile(blockDescriptorPath);
    }
}

package it.unipi.dii.aide.mircv.application.config;

public class VocabularyConfig {

    private final String vocabularyPath;

    private final String partialVocabularyDir;

    private String blockDescriptorsPath;

    private final boolean compressionEnabled;


    public String getPathToVocabularyFile() {
        return vocabularyPath;
    }

    public VocabularyConfig(String vocabularyPath, String partialVocabularyDir, boolean compressionEnabled) {
        this.vocabularyPath = vocabularyPath;
        this.partialVocabularyDir = partialVocabularyDir;
        this.compressionEnabled = compressionEnabled;
    }

    public String getBlockDescriptorsPath() {
        return blockDescriptorsPath;
    }

    public void setBlockDescriptorsPath(String blockDescriptorsPath) {
        this.blockDescriptorsPath = blockDescriptorsPath;
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public String getPathToPartialVocabularyDir(int i) {
        return partialVocabularyDir + "/partial_vocabulary" + i;
    }

    public String getPathToVocabularyDir() {
        return partialVocabularyDir;
    }

    public String getPartialVocabularyDir() {
        return partialVocabularyDir;
    }
}

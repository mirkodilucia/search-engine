package it.unipi.dii.aide.mircv.application.config;

import java.io.Serializable;

public class Config implements Serializable {

    private String testDir;

    private String debugDir;
    private boolean debugEnabled;

    public DatasetConfig datasetConfig;

    public PreprocessConfig preprocessConfig;

    public VocabularyConfig vocabularyConfig;
    public InvertedIndexConfig invertedIndexConfig;

    public ScorerConfig scorerConfig;

    public CollectionConfig collectionConfig;

    public PartialDirectoryConfig partialDirectoryConfig;

    public BlockDescriptorConfig blockDescriptorConfig;

    public DocumentIndexConfig documentIndexConfig;

    public Config(){

    }


    public Config(
        DatasetConfig datasetConfig,
        PreprocessConfig preprocessConfig,
        VocabularyConfig vocabularyConfig,
        InvertedIndexConfig invertedIndexConfig,
        ScorerConfig scorerConfig,
        CollectionConfig collectionConfig,
        PartialDirectoryConfig partialDirectoryConfig,
        BlockDescriptorConfig blockDescriptorConfig,
        DocumentIndexConfig documentIndexConfig,
        String testDir,
        String debugDir,
        boolean debugEnabled
    ) {
        this.datasetConfig = datasetConfig;
        this.preprocessConfig = preprocessConfig;
        this.vocabularyConfig = vocabularyConfig;
        this.invertedIndexConfig = invertedIndexConfig;
        this.scorerConfig = scorerConfig;
        this.collectionConfig = collectionConfig;
        this.partialDirectoryConfig = partialDirectoryConfig;
        this.blockDescriptorConfig = blockDescriptorConfig;
        this.documentIndexConfig = documentIndexConfig;

        this.testDir = testDir;
        this.debugDir = debugDir;
        this.debugEnabled = debugEnabled;
    }

    public boolean isCompressionEnabled() {
        return blockDescriptorConfig.isCompressionEnabled();
    }

    public boolean isDebugEnabled() {
        return this.debugEnabled;
    }

    public String getDebugFolder() {
        return this.debugDir;
    }



    // Getters and Setters

    public String getTestDir() {
        return testDir;
    }

    public void setTestDir(String testDir) {
        this.testDir = testDir;
    }

    public String getDebugDir() {
        return debugDir;
    }

    public void setDebugDir(String debugDir) {
        this.debugDir = debugDir;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public PreprocessConfig getPreprocessConfig() {
        return preprocessConfig;
    }

    public void setPreprocessConfig(PreprocessConfig preprocessConfig) {
        this.preprocessConfig = preprocessConfig;
    }

    public VocabularyConfig getVocabularyConfig() {
        return vocabularyConfig;
    }

    public void setVocabularyConfig(VocabularyConfig vocabularyConfig) {
        this.vocabularyConfig = vocabularyConfig;
    }

    public InvertedIndexConfig getInvertedIndexConfig() {
        return invertedIndexConfig;
    }

    public DatasetConfig getDatasetConfig() {
        return datasetConfig;
    }

    public void setDatasetConfig(DatasetConfig datasetConfig) {
        this.datasetConfig = datasetConfig;
    }

    public void setInvertedIndexConfig(InvertedIndexConfig invertedIndexConfig) {
        this.invertedIndexConfig = invertedIndexConfig;
    }

    public ScorerConfig getScorerConfig() {
        return scorerConfig;
    }

    public void setScorerConfig(ScorerConfig scorerConfig) {
        this.scorerConfig = scorerConfig;
    }

    public CollectionConfig getCollectionConfig() {
        return collectionConfig;
    }

    public PartialDirectoryConfig getPartialDirectoryConfig() {
        return partialDirectoryConfig;
    }

    public BlockDescriptorConfig getBlockDescriptorConfig() {
        return blockDescriptorConfig;
    }

    public DocumentIndexConfig getDocumentIndexConfig() {
        return documentIndexConfig;
    }

    public void setPartialDirectoryConfig(PartialDirectoryConfig partialDirectoryConfig) {
        this.partialDirectoryConfig = partialDirectoryConfig;
    }

    public void setBlockDescriptorConfig(BlockDescriptorConfig blockDescriptorConfig) {
        this.blockDescriptorConfig = blockDescriptorConfig;
    }

    public void setDocumentIndexConfig(DocumentIndexConfig documentIndexConfig) {
        this.documentIndexConfig = documentIndexConfig;
    }


    public void setCollectionConfig(CollectionConfig collectionConfig) {
        this.collectionConfig = collectionConfig;
    }

    public void cleanUpVocabulary() {
        this.vocabularyConfig.cleanUp();
    }

    public void cleanUpInvertedIndex() {
        this.invertedIndexConfig.cleanUp();
    }
}
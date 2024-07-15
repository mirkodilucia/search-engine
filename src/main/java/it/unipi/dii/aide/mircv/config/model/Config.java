package it.unipi.dii.aide.mircv.config.model;

import it.unipi.dii.aide.mircv.utils.FileHandler;

import java.io.File;

public class Config {
    public boolean compressedReading;
    public boolean debug;
    private final String debugDir;
    private final String testDir;

    private final String documentIndexFile;

    private final String datasetPath;

    private PreprocessConfig preprocessConfig;

    private VocabularyConfig vocabularyConfig;

    private BlockDescriptorConfig blockDescriptorConfig;

    public InvertedIndexConfig invertedIndexConfig;

    public PartialResultsConfig partialResultsConfig;

    public ScorerConfig scorerConfig;
    private String compressedCollectionFile;

    public Config(String documentIndexFile, String datasetPath, String testDir, String debugDir, boolean debugEnabled) {
        super();

        this.documentIndexFile = documentIndexFile;
        this.datasetPath = datasetPath;
        this.testDir = testDir;
        this.debugDir = debugDir;

    }

    public Config() {
        this.vocabularyConfig = new VocabularyConfig(
                "data/vocabulary.dat",
                "data/documentIndexState.dat"
        );
        this.preprocessConfig = new PreprocessConfig(
                "data_resources/stopwords.dat",
                true,
                true);

        this.invertedIndexConfig = new InvertedIndexConfig(
                "data/indexes/inverted_index_freqs.dat",
                "data/indexes/inverted_index_docs.dat"
        );

        this.partialResultsConfig = new PartialResultsConfig(
                "data/indexes",
                "data/indexes",
                "data/indexes");

        this.blockDescriptorConfig = new BlockDescriptorConfig(
                "data/block_descriptors.dat",
                true);

        this.compressedReading = false;
        this.compressedCollectionFile = "data/compressed_collection.tar.gz";

        this.scorerConfig = new ScorerConfig(true);

        this.documentIndexFile = "data/documents/document_index.dat";
        this.datasetPath = "data/dataset.tsv";
        this.testDir = "data/test";
        this.debugDir = "data/debug";
        this.debug = false;
    }

    public String getVocabularyPath() {
        return this.vocabularyConfig.getVocabularyPath();
    }

    public Config setScorerConfig(ScorerConfig config) {
        this.scorerConfig = config;
        return this;
    }

    public ScorerConfig getScorerConfig() {
        return this.scorerConfig;
    }

    public PreprocessConfig getPreprocessConfig() {
        return this.preprocessConfig;
    }

    public BlockDescriptorConfig getBlockDescriptorConfig() {
        return this.blockDescriptorConfig;
    }

    public String getDocumentIndexFile() {
        return documentIndexFile;
    }

    public String getPartialIndexesDocumentsPath() {
        return this.partialResultsConfig.getDocIdDir() + "/partial_doc_ids";
    }

    public String getPartialVocabularyPath() {
        return this.partialResultsConfig.getPartialVocabularyDir() + "/partial_vocabulary";
    }

    public String getPartialIndexesFrequenciesPath() {
        return this.partialResultsConfig.getFrequencyDir() + "/partial_freqs";
    }


    public Config setVocabularyPath(VocabularyConfig vocabularyConfig) {
        this.vocabularyConfig = vocabularyConfig;
        return this;
    }

    public Config setPartialIndexConfig(InvertedIndexConfig config) {
        this.invertedIndexConfig = config;
        return this;
    }

    public Config setPartialResultConfig(PartialResultsConfig config) {
        this.partialResultsConfig = config;
        return this;
    }

    public Config setBlockDescriptorConfig(BlockDescriptorConfig config) {
        this.blockDescriptorConfig = config;
        return this;
    }

    public void setInvertedIndexConfig(InvertedIndexConfig invertedIndexConfig) {
        this.invertedIndexConfig = invertedIndexConfig;
    }

    public Config setBlockDescriptorPath(BlockDescriptorConfig blockDescriptorConfig) {
        this.blockDescriptorConfig = blockDescriptorConfig;
        return this;
    }

    public String getBlockDescriptorsPath() {
        return this.blockDescriptorConfig.getBlockDescriptorsPath();
    }

    public String getCollectionStatisticsPath() {
        return this.vocabularyConfig.getCollectionStatisticsPath();
    }

    public String getDebugPath() {
        return this.debugDir;
    }

    public String getInvertedIndexDocs() {
        return this.invertedIndexConfig.getInvertedIndexDocs();
    }

    public String getInvertedIndexFreqsFile() {
        return this.invertedIndexConfig.getInvertedIndexFreqsFile();
    }

    public void setScorerConfig(boolean maxScoreEnabled, boolean compressionEnabled, boolean stopwordRemoval) {
        if (this.scorerConfig == null) {
            this.scorerConfig = new ScorerConfig(maxScoreEnabled);
        }
        this.scorerConfig.setMaxScoreEnabled(maxScoreEnabled);

        if (this.blockDescriptorConfig == null) {
            this.blockDescriptorConfig = new BlockDescriptorConfig(
                    "data/block_descriptors.txt.dat",
                    compressionEnabled);
        }
        this.blockDescriptorConfig.setCompressionEnabled(compressionEnabled);

        if (this.preprocessConfig == null) {
            this.preprocessConfig = new PreprocessConfig(
                    "data_resources/stopwords.dat",
                    true,
                    true
            );
        }
        this.preprocessConfig.setStemStopRemovalEnabled(stopwordRemoval);
    }

    public Config setPreprocessConfig(PreprocessConfig preprocessConfig) {
        this.preprocessConfig = preprocessConfig;
        return this;
    }

    public boolean getCompressionEnabled() {
        return this.blockDescriptorConfig.getCompressionEnabled();
    }

    public void cleanup() {
        FileHandler.removeFile(vocabularyConfig.getVocabularyPath());
        FileHandler.removeFile(documentIndexFile);
        FileHandler.removeFile(invertedIndexConfig.getInvertedIndexDocs());
        FileHandler.removeFile(invertedIndexConfig.getInvertedIndexFreqsFile());
        FileHandler.removeFile(blockDescriptorConfig.getBlockDescriptorsPath());
    }

    public String getDatasetPath() {
        return this.datasetPath;
    }

    public String getCompressedCollectionPath() {
        return this.compressedCollectionFile;
    }
}

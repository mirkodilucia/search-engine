package it.unipi.dii.aide.mircv.config;

public class Config {

    public boolean compression;

    public boolean debug;

    public PreprocessConfig preprocessConfig;

    private BlockDescriptorConfig blockDescriptorConfig;

    public InvertedIndexConfig invertedIndexConfig;

    public PartialResultsConfig partialResultsConfig;

    public ScorerConfig scorerConfig;
    private String vocabularyPath;

    private final String debugDir;
    private final String testDir;
    private final boolean debugEnabled;

    public Config() {
        this.preprocessConfig = new PreprocessConfig(
                "data_resources/stopwords.dat",
                true,
                true);
        this.partialResultsConfig = new PartialResultsConfig(
                "data/indexes",
                "data/indexes",
                "data/indexes");

        this.blockDescriptorConfig = new BlockDescriptorConfig(
                "data/block_descriptors.dat",
                true);

        this.scorerConfig = new ScorerConfig(true);

        this.testDir = "data_test/test";
        this.debugDir = "data_test/debug";
        this.debugEnabled = true;
    }

    public String getVocabularyPath() {
        return this.vocabularyPath;
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

    public String getPartialIndexesDocumentsPath() {
        return this.partialResultsConfig.getDocIdDir() + "/partial_doc_ids";
    }

    public String getPartialVocabularyPath() {
        return this.partialResultsConfig.getPartialVocabularyDir() + "/partial_vocabulary";
    }

    public String getPartialIndexesFrequenciesPath() {
        return this.partialResultsConfig.getFrequencyDir() + "/partial_freqs";
    }


    public Config setVocabularyPath(String s) {
        this.vocabularyPath = s;
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

    public InvertedIndexConfig getInvertedIndexConfig() {
        return this.invertedIndexConfig;
    }

    public void setInvertedIndexConfig(InvertedIndexConfig invertedIndexConfig) {
        this.invertedIndexConfig = invertedIndexConfig;
    }
}

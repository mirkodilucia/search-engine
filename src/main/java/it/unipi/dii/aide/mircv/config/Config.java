package it.unipi.dii.aide.mircv.config;

public class Config {

    public boolean compression;

    public boolean debug;

    private PreprocessConfig preprocessConfig;

    private VocabularyConfig vocabularyConfig;

    private BlockDescriptorConfig blockDescriptorConfig;

    public InvertedIndexConfig invertedIndexConfig;

    public PartialResultsConfig partialResultsConfig;

    public ScorerConfig scorerConfig;

    private final String debugDir;
    private final String testDir;
    private final boolean debugEnabled;

    public Config() {
        this.vocabularyConfig = new VocabularyConfig(
                "data/vocabulary"
        );
        this.preprocessConfig = new PreprocessConfig(
                "data_resources/stopwords.dat",
                true,
                true);

        this.partialResultsConfig = new PartialResultsConfig(
                "data/indexes",
                "data/indexes",
                "data/indexes");

        this.blockDescriptorConfig = new BlockDescriptorConfig(
                "data/block_descriptors.txt.dat",
                true);

        this.scorerConfig = new ScorerConfig(true);

        this.testDir = "data_test/test";
        this.debugDir = "data_test/debug";
        this.debugEnabled = true;
    }

    public String getVocabularyPath() {
        return this.vocabularyConfig.getVocabularyPath();
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

    public InvertedIndexConfig getInvertedIndexConfig() {
        return this.invertedIndexConfig;
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
}

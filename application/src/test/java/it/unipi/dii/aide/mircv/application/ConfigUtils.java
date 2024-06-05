package it.unipi.dii.aide.mircv.application;

import it.unipi.dii.aide.mircv.application.config.*;

public class ConfigUtils {


    public static Config getConfig(String basePath) {

        Config config = new Config();
        config.setBlockDescriptorConfig(new BlockDescriptorConfig("data/blockDescriptors", true));

        config.setVocabularyConfig(new VocabularyConfig("/vocabulary", "/frequencies" , "/docids", "data/vocabulary"));
        config.setDocumentIndexConfig(new DocumentIndexConfig("data/documentIndex"));
        config.setPartialResultsConfig(new PartialResultsConfig("data/partial_vocabulary", "data/partial_frequencies", "data/partial_docids"));
        config.setInvertedIndexConfig(new InvertedIndexConfig("data/invertedIndexFreqs", "data/invertedIndexDocs"));
        config.setScorerConfig(new ScorerConfig(true));
        config.setPreprocessConfig(new PreprocessConfig("../resources/stopwords.dat", true, true));
        config.setDatasetConfig(new DatasetConfig("../resources/dataset/collection.tar.gz"));
        config.setCollectionConfig(new CollectionConfig("../test/data/" + basePath + "/rawCollection", "../test/data/" + basePath + "/compressedCollection", "../test/data/" + basePath + "/collectionStats"));

        return config;


    }

}

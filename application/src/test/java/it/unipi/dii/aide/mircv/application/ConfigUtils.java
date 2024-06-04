package it.unipi.dii.aide.mircv.application;

import it.unipi.dii.aide.mircv.application.config.*;

public class ConfigUtils {


    public static Config getConfig(String basePath) {

        Config config = new Config();
        config.setBlockDescriptorConfig(new BlockDescriptorConfig("../test/data/" + basePath + "/blockDescriptors", true));
        config.setVocabularyConfig(new VocabularyConfig("../test/data/" + basePath + "/vocabulary", "../test/data/" + basePath + "/vocabularyFreqs", "../test/data/" + basePath + "/vocabularyDocId"));
        config.setDocumentIndexConfig(new DocumentIndexConfig("../test/data/" + basePath + "/documentIndex"));
        config.setPartialDirectoryConfig(new PartialDirectoryConfig("../test/data/" + basePath + "/partialInvertedFrequenciesPathDir", "../test/data/" + basePath + "/partialInvertedIndexDocIdPathDir", "../test/data/" + basePath + "/partialVocabularyDir", "../test/data/" + basePath + "/frequencyDir", "../test/data/" + basePath + "/docIdDir"));
        config.setInvertedIndexConfig(new InvertedIndexConfig("../test/data/" + basePath + "/invertedIndexFreqs", "../test/data/" + basePath + "/invertedIndexDocId"));
        config.setScorerConfig(new ScorerConfig(true));
        config.setPreprocessConfig(new PreprocessConfig("../resources/stopwords.dat", true, true));
        config.setDatasetConfig(new DatasetConfig("../resources/dataset/collection.tar.gz"));
        config.setCollectionConfig(new CollectionConfig("../test/data/" + basePath + "/rawCollection", "../test/data/" + basePath + "/compressedCollection", "../test/data/" + basePath + "/collectionStats"));

        return config;


    }

}

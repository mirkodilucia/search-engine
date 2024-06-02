package it.unipi.dii.aide.mircv.application;

import it.unipi.dii.aide.mircv.application.config.*;

public class ConfigUtils {

    public static Config getConfig(String basePath) {
        return new Config(
                new DatasetConfig(
                        "../resources/dataset/collection.tar.gz"
                ),

                new PreprocessConfig(
                        "../resources/stopwords.dat",
                        true,
                        true
                ),
                new VocabularyConfig(
                        "../test/data/" + basePath + "/vocabulary",
                        "../test/data/" + basePath + "/blockDescriptors",
                        true
                ),
                new InvertedIndexConfig(
                        "../test/data/"  + basePath + "/blockDescriptors",
                        "../test/data/" + basePath + "/invertedIndexDocs",
                        "../test/data/" + basePath + "/invertedIndexFreqs",
                        "../test/data/" + basePath + "/documentIndex",
                        "../test/data/" + basePath +"/partialInvertedFreqs",
                        "../test/data/" + basePath + "/partialInvertedDocs"
                ),
                new ScorerConfig(
                        true
                ),
                new CollectionConfig(
                        "../test/data/" + basePath + "/rawCollection",
                        "../test/data/ " + basePath + "/compressedCollection",
                        "../test/data/" + basePath + "/collectionStats"
                ),
                "../test/data/" + basePath,
                "../test/data/" + basePath,
                true
        );
    }

}

package it.unipi.dii.aide.mircv.application.config;

public class CollectionConfig {

    private String rawCollectionPath;
    private String compressedCollectionPath;

    private String collectionStatisticsPath;

    public CollectionConfig(String rawCollectionPath,
                            String compressedCollectionPath,
                            String collectionStatisticsPath) {
        this.rawCollectionPath = rawCollectionPath;
        this.compressedCollectionPath = compressedCollectionPath;
        this.collectionStatisticsPath = collectionStatisticsPath;
    }

    public String getRawCollectionPath() {
        return rawCollectionPath;
    }

    public String getCollectionStatisticsPath() {
        return collectionStatisticsPath;
    }

    public void setCollectionStatisticsPath(String collectionStatisticsPath) {
        this.collectionStatisticsPath = collectionStatisticsPath;
    }

    public String getCompressedCollectionPath() {
        return compressedCollectionPath;
    }

    public void setRawCollectionPath(String rawCollectionPath) {
        this.rawCollectionPath = rawCollectionPath;
    }

    public void setCompressedCollectionPath(String compressedCollectionPath) {
        this.compressedCollectionPath = compressedCollectionPath;
    }
}

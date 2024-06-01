package it.unipi.dii.aide.mircv.application.config;

public class DatasetConfig {
    private String datasetPath;

    public DatasetConfig(String datasetPath) {
        this.datasetPath = datasetPath;
    }

    public String getDatasetPath() {
        return datasetPath;
    }

    public void setDatasetPath(String datasetPath) {
        this.datasetPath = datasetPath;
    }
}

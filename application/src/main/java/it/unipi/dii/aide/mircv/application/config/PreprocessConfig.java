package it.unipi.dii.aide.mircv.application.config;

public class PreprocessConfig {

    private final String stopwordsPath;
    private final boolean removeStopwords;
    private boolean stemmerEnabled;

    public PreprocessConfig(String stopwordsPath, boolean removeStopwords, boolean stemmerEnabled) {
        this.stopwordsPath = stopwordsPath;
        this.removeStopwords = removeStopwords;
        this.stemmerEnabled = stemmerEnabled;
    }

    public String getStopwordsPath() {
        return stopwordsPath;
    }

    public boolean isRemoveStopwordsEnabled() {
        return removeStopwords;
    }

    public boolean isStemmerEnabled() {
        return stemmerEnabled;
    }

    public void setStemStopRemovalEnabled(boolean stemmerEnabled) {
        this.stemmerEnabled = stemmerEnabled;
    }
}

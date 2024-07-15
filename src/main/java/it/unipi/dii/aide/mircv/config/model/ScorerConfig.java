package it.unipi.dii.aide.mircv.config.model;

public class ScorerConfig {

    private boolean maxScoreEnabled;

    public ScorerConfig(boolean maxScoreEnabled) {
        this.maxScoreEnabled = maxScoreEnabled;
    }

    public void setMaxScoreEnabled(boolean maxScoreEnabled) {
        this.maxScoreEnabled = maxScoreEnabled;
    }

    public boolean isMaxScoreEnabled() {
        return maxScoreEnabled;
    }
}

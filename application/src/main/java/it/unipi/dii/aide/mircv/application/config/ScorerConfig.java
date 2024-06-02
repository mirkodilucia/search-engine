package it.unipi.dii.aide.mircv.application.config;

public class ScorerConfig {

    private boolean maxScoreEnabled;

    public ScorerConfig(boolean maxScoreEnabled) {
        this.maxScoreEnabled = maxScoreEnabled;
    }

    public boolean isMaxScoreEnabled() {
        return maxScoreEnabled;
    }

    public void setMaxScoreEnabled(boolean maxScoreEnabled) {
        this.maxScoreEnabled = maxScoreEnabled;
    }
}

package it.unipi.dii.aide.mircv.config;

public class Config {

    public boolean compression;

    public boolean debug;

    public ScorerConfig scorerConfig;

    public Config() {
        this.scorerConfig = new ScorerConfig(true);
    }
}

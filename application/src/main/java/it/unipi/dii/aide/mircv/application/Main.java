package it.unipi.dii.aide.mircv.application;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.config.ConfigLoader;
import it.unipi.dii.aide.mircv.document.DocumentManager;

public class Main {
    public static void main(String[] args) {
        Config config = ConfigLoader.load();

        DocumentManager dp = DocumentManager.with(config);
    }
}
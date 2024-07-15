package it.unipi.dii.aide.mircv.indexer;

import it.unipi.dii.aide.mircv.FlagManager;
import it.unipi.dii.aide.mircv.config.ConfigLoader;
import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.document.DocumentManager;
import it.unipi.dii.aide.mircv.indexer.merger.Merger;
import it.unipi.dii.aide.mircv.indexer.spimi.Spimi;

import java.io.IOException;

public class Indexer {

    public static void main(String[] args) {
        Config configLoaded = ConfigLoader.load();
        Config config = FlagManager.parseArgs(configLoaded, args);

        try {
            DocumentManager dm = DocumentManager.with(config);
            dm.start();

            int spimiIndex = Spimi.with(config).executeSpimi();

            Merger merger = Merger.with(config);
            merger.mergeIndexes(spimiIndex);
        }catch (IOException e){

            e.printStackTrace();
        }
    }
}

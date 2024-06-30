package it.unipi.dii.aide.mircv.indexer;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.document.DocumentManager;
import it.unipi.dii.aide.mircv.indexer.merger.Merger;
import it.unipi.dii.aide.mircv.indexer.spimi.Spimi;

import java.io.IOException;

public class Indexer {

    public static void main(String[] args) {
        Config config = new Config();

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

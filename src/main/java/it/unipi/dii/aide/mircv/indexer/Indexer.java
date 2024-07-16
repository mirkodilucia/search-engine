package it.unipi.dii.aide.mircv.indexer;

import it.unipi.dii.aide.mircv.FlagManager;
import it.unipi.dii.aide.mircv.config.ConfigLoader;
import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.document.DocumentManager;
import it.unipi.dii.aide.mircv.indexer.merger.Merger;
import it.unipi.dii.aide.mircv.indexer.spimi.Spimi;

import java.io.IOException;
import java.util.Arrays;

public class Indexer {

    public static void main(String[] args) {
        Config configLoaded = ConfigLoader.load();
        Config config = FlagManager.parseArgs(configLoaded, args);

        try {
            DocumentManager dm = DocumentManager.with(config);
            dm.start();

            System.out.println("Indexing started with parameters: " + Arrays.toString(args));
            long start = System.currentTimeMillis();

            int spimiIndex = Spimi.with(config).executeSpimi();

            long spimiTime = System.currentTimeMillis();
            formatTime(start, spimiTime, "Spimi");

            Merger merger = Merger.with(config);


            merger.mergeIndexes(spimiIndex);

            long stop = System.currentTimeMillis();
            formatTime(spimiTime, stop, "Merging");
            formatTime(start, stop, "Creation of inverted index");

            long fullTime = stop - start;

            System.out.println("Total time: " + fullTime / 1000 + " seconds");

        }catch (IOException e){

            e.printStackTrace();
        }
    }

    /**
     * formats the prints used when an indexing operation is completed
     *
     * @param start     the start time
     * @param end       the stop time
     * @param operation the operation done
     */
    private static void formatTime(long start, long end, String operation) {
        int minutes = (int) ((end - start) / (1000 * 60));
        int seconds = (int) ((end - start) / 1000) % 60;
        if (seconds < 10)
            System.out.println(operation + " done in " + minutes + ":0" + seconds + " minutes");
        else
            System.out.println(operation + " done in " + minutes + ":" + seconds + " minutes");
    }
}

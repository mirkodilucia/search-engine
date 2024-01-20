package it.unipi.dii.aide.mircv;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.config.ConfigLoader;
import it.unipi.dii.aide.mircv.document.DocumentManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {

    private static Config config;

    public static void main(String[] args) {
        config = ConfigLoader.load();

        DocumentManager dp = DocumentManager.with(config);
        dp.initialize();

        long start = System.currentTimeMillis();
        // Aggiungere SPIMI
        long stop = System.currentTimeMillis();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter("data/indexerStatistics.tsv", true));) {
            long docidSize = Files.size(Paths.get(config.invertedIndexDocs));
            long freqSize = Files.size(Paths.get(config.invertedIndexFreqs));
            long vocabularySize = Files.size(Paths.get(config.vocabularyFileName));
            long docIndexSize = Files.size(Paths.get(config.documentIndexPath));

            long fullTime = stop - start;
            String stats = Arrays.toString(args) + '\t' + fullTime + '\t' + docidSize + '\t' + freqSize + '\t' + vocabularySize + '\t' + docIndexSize + '\n';
            writer.write(stats);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
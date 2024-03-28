package it.unipi.dii.aide.mircv.application;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.config.ConfigLoader;
import it.unipi.dii.aide.mircv.application.config.DocumentManager;
import it.unipi.dii.aide.mircv.application.indexer.spimi.Spimi;

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

        int numIndexes = Spimi.with(config).executeSpimi();
        if(numIndexes <= 0){
            System.out.println("An error occurred: no partial indexes.");
            return;
        }

        //Merger2.with(config, numIndexes).merge();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter("data/indexerStatistics.tsv", true));) {
            long docidSize = Files.size(Paths.get(config.getPathToInvertedIndexDocs()));
            long freqSize = Files.size(Paths.get(config.getPathToInvertedIndexFreqs()));
            long vocabularySize = Files.size(Paths.get(config.getVocabularyFileName()));
            long docIndexSize = Files.size(Paths.get(config.getDocumentIndexPath()));

            long fullTime = 0; //stop - start;
            String stats = Arrays.toString(args) + '\t' + fullTime + '\t' + docidSize + '\t' + freqSize + '\t' + vocabularySize + '\t' + docIndexSize + '\n';
            writer.write(stats);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
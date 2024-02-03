package it.unipi.dii.aide.mircv.application.indexer.spimi;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.DocumentIndexEntry;
import it.unipi.dii.aide.mircv.application.data.PostingList;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class Spimi {

    private static long MEMORY_LIMIT = 0;
    private int numIndexes = 0;
    private long numPostings = 0;

    private Config config;

    private Spimi(Config config) {
        this.config = config;
        numIndexes = 0;
        DocumentIndexEntry.reset();

        MEMORY_LIMIT = Math.round(Runtime.getRuntime().totalMemory() * 0.2);

    }

    public void executeSpimi() {
        try (BufferedReader bufferReader = initBuffer(config.isCompressionEnabled())) {
            boolean allDocumentsProcessed = false;
            int documentId = 1;
            int documentsLength = 0;
            boolean writeResult = false;





        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private HashMap<String, PostingList> spiiIteration(BufferedReader bufferedReader) throws IOException {
        HashMap<String, PostingList> index = new HashMap<>();
            while (Runtime.getRuntime().freeMemory() > MEMORY_LIMIT) {
                String line;

                if ((line = bufferedReader.readLine()) == null) {
                    // TODO: Impostare il listener
                    allDocumentsProcessed = true;
                    break;
                }

                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split("\t");

                TextDocument document = new TextDocument(fields[0], fields[1].replaceAll("[^\\x00-\\x7F]", ""));


            }
        return index;
    }

    private BufferedReader initBuffer(boolean compressed) throws IOException {
        if(compressed) { //read from compressed collection
            TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(PATH_COMPRESSED_COLLECTION)));
            tarInput.getNextTarEntry();
            return new BufferedReader(new InputStreamReader(tarInput, StandardCharsets.UTF_8));
        }

        return Files.newBufferedReader(Paths.get(config.getRawCollectionPath()), StandardCharsets.UTF_8);
    }

    public static Spimi with(Config config) {
        return new Spimi(config);
    }

}

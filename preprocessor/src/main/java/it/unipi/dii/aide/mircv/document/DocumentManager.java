package it.unipi.dii.aide.mircv.document;

import java.io.File;
import java.util.ArrayList;

import it.unipi.dii.aide.mircv.utils.FileUtils;
import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.document.data.PlainDocument;

public class DocumentManager {

    private static final boolean REMOVE_STOPWORDS_ENABLED = true;
    private static Config configuration;

    private Config config;
    private static DocumentManager INSTANCE = null;
    private ArrayList<String> filePaths;
    private ArrayList<PlainDocument> documents;

    private DocumentManager() {
        filePaths = new ArrayList<>();
        scanDirectory();

        documents = new ArrayList<>();
        for (String path : filePaths) {
            documents.add(load(path));
        }
    }

    private ArrayList<String> scanDirectory() {
        // Load all files from the directory
        File folder = new File(configuration.datasetPath);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null) {
            System.out.println("No files found in the directory");
            return null;
        }

        // Create an array of file paths
        for (int i = 0; i < listOfFiles.length; i++) {
            filePaths.add(listOfFiles[i].getPath());
        }

        return filePaths;
    }

    public PlainDocument load(String path) {
        String plainText = FileUtils.readFile(path);
        return new PlainDocument("docId", plainText);
    }

    public PlainDocument process(PlainDocument doc) {
        doc.cleanText();
        doc.tokenize();

        if (REMOVE_STOPWORDS_ENABLED) {
            doc.removeStopwords();
            String[] stems = doc.stem();
        }

        return doc;
    }

    public static DocumentManager with(Config config) {
        configuration = config;
        if(INSTANCE == null) {
            INSTANCE = new DocumentManager();
        }

        return INSTANCE;
    }
}

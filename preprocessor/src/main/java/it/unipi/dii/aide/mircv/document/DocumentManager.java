package it.unipi.dii.aide.mircv.document;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import it.unipi.dii.aide.mircv.utils.FileUtils;
import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.document.data.PlainDocument;

public class DocumentManager {

    private static Config configuration;
    private static DocumentManager INSTANCE = null;
    private final ArrayList<String> filePaths;
    private final ArrayList<PlainDocument> documents;

    private DocumentManager() {
        filePaths = new ArrayList<>();
        scanDirectory();

        documents = new ArrayList<>();
        for (String path : filePaths) {
            documents.add(load(path));
        }

        for (PlainDocument doc : documents) {
            process(doc);
        }

        System.out.println("Loaded " + documents.size() + " documents");
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
        for (File listOfFile : listOfFiles) {
            filePaths.add(listOfFile.getPath());
        }

        return filePaths;
    }

    public PlainDocument load(String path) {
        String plainText = FileUtils.readFile(path);
        String uuid = UUID.randomUUID().toString();
        return new PlainDocument(configuration, uuid, plainText);
    }

    public PlainDocument process(PlainDocument doc) {
        doc.cleanText();
        doc.tokenize();

        if (configuration.removeStopwords) {
            doc.removeStopwords();

        }

        doc.stem();
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

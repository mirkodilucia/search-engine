package it.unipi.dii.aide.mircv.application.config;

import it.unipi.dii.aide.mircv.application.data.InitialDocument;
import it.unipi.dii.aide.mircv.application.utils.FileUtils;

import java.util.ArrayList;
import java.util.UUID;

public class DocumentManager {

    private static Config configuration;
    private static DocumentManager INSTANCE = null;
    private final ArrayList<String> filePaths;
    private final ArrayList<InitialDocument> documents;

    private DocumentManager() {
        filePaths = new ArrayList<>();
        documents = new ArrayList<>();

        for (InitialDocument doc : documents) {
            process(doc);
            doc.writeFileString();
        }

        System.out.println("Loaded " + documents.size() + " documents");
    }

    public InitialDocument load(String path) {
        String plainText = FileUtils.readFile(path);
        String uuid = UUID.randomUUID().toString();
        return new InitialDocument(configuration, uuid, plainText);
    }

    public InitialDocument process(InitialDocument doc) {
        doc.cleanText();
        doc.tokenize();

        if (configuration.preprocessConfig.isRemoveStopwordsEnabled()) {
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

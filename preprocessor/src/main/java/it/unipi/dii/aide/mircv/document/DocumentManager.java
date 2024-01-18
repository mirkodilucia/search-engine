package it.unipi.dii.aide.mircv.document;

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

        documents = new ArrayList<>();

        for (PlainDocument doc : documents) {
            process(doc);
            doc.writeFileString();

        }

        System.out.println("Loaded " + documents.size() + " documents");
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

    public void initialize() {
        FileUtils.removeFile(configuration.documentIndexPath);
        FileUtils.removeFile(configuration.vocabularyFileName);
        FileUtils.removeFile(configuration.invertedIndexDocs);
        FileUtils.removeFile(configuration.invertedIndexFreqs);
        FileUtils.removeFile(configuration.blockDescriptorsPath);

        FileUtils.deleteFolder(configuration.partialVocabularyDir);
        FileUtils.deleteFolder(configuration.docidsDir);
        FileUtils.deleteFolder(configuration.frequencyDir);
        FileUtils.deleteFolder(configuration.debugDir);

        FileUtils.createFolder(configuration.partialVocabularyDir);
        FileUtils.createFolder(configuration.docidsDir);
        FileUtils.createFolder(configuration.frequencyDir);
    }
}

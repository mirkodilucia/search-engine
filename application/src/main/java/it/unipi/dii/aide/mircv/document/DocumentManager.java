package it.unipi.dii.aide.mircv.document;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.document.preprocess.FinalDocument;
import it.unipi.dii.aide.mircv.document.preprocess.InitialDocument;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DocumentManager {

    private Config configuration;

    private DocumentManager(Config config) {
        this.configuration = config;
    }

    public static DocumentManager with(Config config) {
        return new DocumentManager(config);
    }

    public void start() throws IOException {
        List<InitialDocument> documents = this.loadDocumentsFromTSV("data/collection.tsv");
        for (InitialDocument document : documents) {
            // Write raw processed document to disk
            document.writeFileString();
        }
    }

    private List<InitialDocument> loadDocumentsFromTSV(String filePath) throws IOException {
        List<InitialDocument> documents = new ArrayList<>();

        // Create document for every 200 lines in the TSV file
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder document = new StringBuilder();
            int lineCount = 0;
            int iteration = 0;
            while ((line = br.readLine()) != null) {
                document.append(line).append("\n");
                lineCount++;
                if (lineCount == 200) {
                    String documentId = "doc" + iteration;
                    documents.add(
                            new InitialDocument(documentId, document.toString())
                    );
                    document = new StringBuilder();
                    lineCount = 0;
                    iteration++;
                }

                // Exit after 10 documents
                if (documents.size() > 10) {
                    break;
                }
            }

            String documentId = "doc" + iteration;
            if (lineCount > 0) {
                documents.add(
                        new InitialDocument(documentId, document.toString())
                );
            }
        }

        return documents;
    }
}

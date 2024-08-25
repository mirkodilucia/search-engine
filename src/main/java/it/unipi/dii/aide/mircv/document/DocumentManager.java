package it.unipi.dii.aide.mircv.document;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.document.preprocess.InitialDocument;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static it.unipi.dii.aide.mircv.document.preprocess.InitialDocument.RAW_COLLECTION_PATH;

public class DocumentManager {

    private Config configuration;

    private DocumentManager(Config config) {
        this.configuration = config;
    }

    public static DocumentManager with(Config config) {
        return new DocumentManager(config);
    }

    public void start() throws IOException {
        //loadFile("data/collection.tsv");
        //List<InitialDocument> documents = this.loadDocumentsFromTSV("data/collection.tsv");

    }

    private void loadFile(String filePath) {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder builder = new StringBuilder();

            while (true) {
                if ((line = br.readLine()) == null) {
                    break;
                }


                String[] split = line.split("\t");
                builder.append(line);
                builder.append("\n");

                if (split[0].equals("20000")) {
                    // Write file with builder content

                    File file = new File(RAW_COLLECTION_PATH + "/collections_20000.tsv");
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                        writer.write(builder.toString());
                    }

                    break;
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                if (lineCount >= 200) {
                    String documentId = "" + iteration;
                    documents.add(
                            new InitialDocument(configuration, documentId, document.toString())
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
                        new InitialDocument(configuration, documentId, document.toString())
                );
            }
        }

        for (InitialDocument document : documents) {
            // Write raw processed document to disk
            document.writeFileString();
        }

        return documents;
    }
}

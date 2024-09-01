package it.unipi.dii.aide.mircv.document;

import it.unipi.dii.aide.mircv.config.model.Config;

import java.io.*;
import java.util.Objects;

public class DocumentIndexState {

    private static long collectionSize = 0;
    private static long vocabularySize = 0;
    private static long totalDocumentLen = 0;

    private static String COLLECTION_STATISTICS_FILE;

    private static DocumentIndexState instance = null;

    private DocumentIndexState(Config config) {
        setupPath(config);
        readFile();
    }

    public static void with(Config config) {
        if (instance == null) {
            instance = new DocumentIndexState(config);
        }
    }

    private void setupPath(Config config) {
        COLLECTION_STATISTICS_FILE = Objects.requireNonNullElse(config.getCollectionStatisticsPath(), "data/collection/collection_statistics.dat");
    }

    /**
     * Read the collection statistics from the file
     * @return true if the file is read correctly, false otherwise
     */
    public static boolean readFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(COLLECTION_STATISTICS_FILE))) {
            collectionSize = ois.readLong();
            vocabularySize = ois.readLong();
            totalDocumentLen = ois.readLong();
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("File not found " + COLLECTION_STATISTICS_FILE );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    /**
     * Write the collection statistics to the file
     * @return true if the file is written correctly, false otherwise
     */
    public static boolean writeFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(COLLECTION_STATISTICS_FILE))) {
            oos.writeLong(collectionSize);
            oos.writeLong(vocabularySize);
            oos.writeLong(totalDocumentLen);
            return true;
        } catch (FileNotFoundException e) {
            // TODO: handle exception with logger
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setCollectionSize(long collectionSize) {
        DocumentIndexState.collectionSize = collectionSize;
    }

    public static void setTotalDocumentLen(long totalDocumentLen) {
        DocumentIndexState.totalDocumentLen = totalDocumentLen;
    }

    public static long getCollectionSize() {
        return collectionSize;
    }

    public static long getVocabularySize() {
        return vocabularySize;
    }

    public static long getTotalDocumentLen() {
        return totalDocumentLen;
    }

    public static void updateVocabularySize(long vocSize) {
        vocabularySize = vocSize;
        writeFile();
    }

    public static boolean updateStatistics(int documentId, int documentsLength) {
        collectionSize = documentId;
        totalDocumentLen = documentsLength;
        return writeFile();
    }
}

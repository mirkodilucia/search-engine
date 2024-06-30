package it.unipi.dii.aide.mircv.document;

import java.io.*;

public class DocumentIndexState {

    private static long collectionSize = 0;
    private static long vocabularySize = 0;
    private static long totalDocumentLen = 0;

    private final static String COLLECTION_STATISTICS_FILE = "data/collection/collection_statistics.dat";

    static{
        if(!readFile()){
            collectionSize = 0;
            vocabularySize = 0;
            totalDocumentLen = 0;
        }
    }

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

    public static void setVocabularySize(long vocabularySize) {
        DocumentIndexState.vocabularySize = vocabularySize;
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

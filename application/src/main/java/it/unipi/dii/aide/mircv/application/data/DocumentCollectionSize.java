package it.unipi.dii.aide.mircv.application.data;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DocumentCollectionSize {

    private static long collectionSize = 0;
    private static long vocabularySize = 0;
    private static long totalDocumentLen = 0;

    public static boolean readFile(String collectionStatisticsPath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(collectionStatisticsPath))) {
            collectionSize = ois.readLong();
            vocabularySize = ois.readLong();
            totalDocumentLen = ois.readLong();
            return true;
        } catch (FileNotFoundException e) {
            // TODO: handle exception with logger
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean writeFile(String collectionStatisticsPath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(collectionStatisticsPath))) {
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

    public static long getCollectionSize() {
        return collectionSize;
    }

    public static long getVocabularySize() {
        return vocabularySize;
    }

    public static long getTotalDocumentLen() {
        return totalDocumentLen;
    }

    public static void updateVocabularySize(long vocSize, String collectionStatisticsPath) {
        vocabularySize = vocSize;
        writeFile(collectionStatisticsPath);
    }

    public static boolean updateStatistics(int documentId, int documentsLength, String collectionStatisticsPath) {
        collectionSize = documentId;
        totalDocumentLen = documentsLength;
        return writeFile(collectionStatisticsPath);
    }
}

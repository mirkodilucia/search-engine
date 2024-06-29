package it.unipi.dii.aide.mircv.application.data;

import it.unipi.dii.aide.mircv.application.config.Config;

import java.io.*;

public class DocumentCollectionSize {

    private static long collectionSize;
    private static long vocabularySize;
    private static long totalDocumentLen;
    private static Config config;
    /**
     * Path to the collection size for testing
     */
    private static String COLLECTION_STATISTICS_PATH = "data/collection_statistics";


    public static void setupCollectionStatisticPath() {
        COLLECTION_STATISTICS_PATH = config.getCollectionConfig().getCollectionStatisticsPath();
    }


    public static void initialize(Config config){
        if(!readFile()) {
            DocumentCollectionSize.config = config;
            setupCollectionStatisticPath();
            collectionSize = 0;
            vocabularySize = 0;
            totalDocumentLen = 0;
        }

    }

    public static boolean readFile() {
        if(COLLECTION_STATISTICS_PATH==null)
            return false;
        File file = new File(COLLECTION_STATISTICS_PATH);

        if(!file.exists()){
            return false;
        }

        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){

            collectionSize = ois.readLong();
            vocabularySize = ois.readLong();
            totalDocumentLen = ois.readLong();

            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeFile() {
        File file = new File(COLLECTION_STATISTICS_PATH);
        if(file.exists())
            if(!file.delete())
                return false;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeLong(collectionSize);
            oos.writeLong(vocabularySize);
            oos.writeLong(totalDocumentLen);
            return true;

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static void setCollectionSize(long collectionSize) {
        DocumentCollectionSize.collectionSize = collectionSize;
    }

    public static void setVocabularySize(long vocabularySize) {
        DocumentCollectionSize.vocabularySize = vocabularySize;
    }

    public static void setTotalDocumentLen(long totalDocumentLen) {
        DocumentCollectionSize.totalDocumentLen = totalDocumentLen;
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

    public static boolean updateStatistics(int documentId, int documentsLength, String collectionStatisticsPath) {
        collectionSize = documentId;
        totalDocumentLen = documentsLength;
        return writeFile();
    }

    /**
     * update the collection size and save the value on disk
     * @param size the new size
     * @return true if write is successful
     */
    public static boolean updateCollectionSize(long size){
        collectionSize = size;
        return writeFile();
    }

    public static boolean updateDocumentsLenght(long len){
        totalDocumentLen = len;
        return writeFile();
    }

    /** needed for testing purposes
     * @param collectionStatisticsPath: path to be set
     */
    public static void setCollectionStatisticsPath(String collectionStatisticsPath) {
        COLLECTION_STATISTICS_PATH = collectionStatisticsPath;

    }

    /** needed for testing purposes
     * @param size: path to be set
     */
    public static void setCollectionSize(int size) { DocumentCollectionSize.collectionSize = size;}


}

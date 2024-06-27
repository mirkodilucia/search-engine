package it.unipi.dii.aide.mircv.indexer.spimi;

import it.unipi.dii.aide.mircv.indexer.model.Posting;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.BaseVocabularyEntry;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseSpimi implements SpimiListener {

    private boolean debugMode = false;

    protected int numIndexes;
    protected int numPostings = 0;
    protected int documentId = 0;

    protected int documentsLength = 0;
    protected boolean allDocumentsProcessed;

    private final static String INVERTED_DOCUMENT_INDEX_FILE_NAME = "data/inverted_index/inverted_index";
    private final static String INVERTED_DOCUMENT_FREQUENCY_FILE_NAME = "data/inverted_index/inverted_index_frequency";
    private static final String VOCABULARY_FILE_NAME = "data/vocabulary/vocabulary";

    protected boolean saveIndexToDisk(HashMap<String, PostingList> index, boolean debugMode) {
        this.debugMode = debugMode;

        System.out.println("Saving index to disk...");

        if (index.isEmpty()) {
            System.out.println("Index is empty, nothing to save.");
            return true;
        }

        index = this.getSortedIndex(index);
        return this.saveInvertedDocuments(index);
    }

    private boolean saveInvertedDocuments(HashMap<String, PostingList> index) {
        try (
                FileChannel docsFchan = FileChannelHandler.open(INVERTED_DOCUMENT_INDEX_FILE_NAME + "_" + numIndexes + ".dat",
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE
                );
                FileChannel freqsFchan = FileChannelHandler.open(INVERTED_DOCUMENT_FREQUENCY_FILE_NAME + "_" + numIndexes + ".dat",
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);

                FileChannel vocabularyFchan = FileChannelHandler.open(VOCABULARY_FILE_NAME + "_" + numIndexes + ".dat",
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE)
        ) {

                MappedByteBuffer docsBuffer = docsFchan.map(FileChannel.MapMode.READ_WRITE, 0, numPostings * 4L);
                MappedByteBuffer freqsBuffer = freqsFchan.map(FileChannel.MapMode.READ_WRITE, 0, numPostings * 4L);

                if (docsBuffer == null || freqsBuffer == null) {
                    System.err.println("Error while mapping buffers to disk.");
                    return false;
                }

                long vocOffset = 0;
                for (PostingList entry : index.values()) {
                    // Create the Vocabulary Entry
                    VocabularyEntry vocEntry = new VocabularyEntry(
                            entry.getTerm(),
                            new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(),
                            new BaseVocabularyEntry.VocabularyMemoryInfo(
                                    docsBuffer.position(),
                                    freqsBuffer.position(),
                                    numPostings * 4,
                                    numPostings * 4)
                            );

                    // Write the Posting List in the inverted index files
                    for (Posting posting : entry.getPostings()) {
                        docsBuffer.putInt(posting.getDocumentId());
                        freqsBuffer.putInt(posting.getFrequency());
                    }

                    // Update the statistics of the Vocabulary Entry, BM25Dl, BM25Tf
                    vocEntry.updateStatistics(entry);
                    vocEntry.updateBM25Statistics(entry.getBM25Tf(), entry.getBM25Dl());
                    vocEntry.updateMemoryIdSize(numPostings * 4);

                    vocOffset = vocEntry.writeEntry(vocOffset, vocabularyFchan);

                    if(debugMode){
                        entry.debugSaveToDisk("partialDOCIDS_" + numIndexes + ".txt", "partialFREQS_" + numIndexes + ".txt", numPostings);
                        vocEntry.debugSaveToDisk("partialVOC_" + numIndexes + ".txt");
                    }
                }

            numIndexes++;
            numPostings = 0;

            return true;
        }catch (IOException e) {
            System.err.println("Error while saving inverted documents to disk: " + e.getMessage());
            return false;
        }
    }

    private HashMap<String, PostingList> getSortedIndex(HashMap<String, PostingList> index) {
        return index.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    @Override
    public void updateDocumentLength(int length) {
        this.documentsLength += length;
    }

    @Override
    public void onSpimiFinished() {
        this.allDocumentsProcessed = true;
    }

    @Override
    public void incrementDocumentId() {
        this.documentId++;
    }
}

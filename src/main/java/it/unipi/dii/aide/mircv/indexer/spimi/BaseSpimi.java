package it.unipi.dii.aide.mircv.indexer.spimi;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.indexer.model.Posting;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.BaseVocabularyEntry;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseSpimi implements SpimiListener {

    private boolean debugMode = false;

    protected int numIndexes;
    protected long numPostings = 0;
    protected int documentId = 0;

    protected int documentsLength = 0;
    protected boolean allDocumentsProcessed;

    protected static String PATH_TO_PARTIAL_VOCABULARIES = "data/vocabulary/vocabulary";
    protected static String PATH_TO_PARTIAL_INDEXES_DOCS = "data/indexes/partial_index_docs_";
    protected static String PATH_TO_PARTIAL_INDEXES_FREQS = "data/indexes/partial_index_freqs_";

    protected final Config config;

    protected BaseSpimi(Config config) {
        this.config = config;
        this.setupPath(config);
    }

    private void setupPath(Config config) {
        PATH_TO_PARTIAL_VOCABULARIES = config.getPartialVocabularyPath();
        PATH_TO_PARTIAL_INDEXES_DOCS = config.getPartialIndexesDocumentsPath();
        PATH_TO_PARTIAL_INDEXES_FREQS = config.getPartialIndexesFrequenciesPath();
    }

    protected boolean saveIndexToDisk(HashMap<String, PostingList> index, boolean debugMode) {
        this.debugMode = debugMode;

        System.out.println("Saving index to disk...");

        if (index.isEmpty()) {
            System.out.println("Index is empty, nothing to save.");
            return true;
        }

        index = this.getSortedIndex(index);
        return this.saveIndexToDisk(index);
    }

    private boolean saveIndexToDisk(HashMap<String, PostingList> index) {
        try (
                FileChannel docsFchan = FileChannelHandler.open(PATH_TO_PARTIAL_INDEXES_DOCS + "_" + numIndexes + ".dat",
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE
                );
                FileChannel freqsFchan = FileChannelHandler.open(PATH_TO_PARTIAL_INDEXES_FREQS + "_" + numIndexes + ".dat",
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);

                FileChannel vocabularyFchan = FileChannelHandler.open(PATH_TO_PARTIAL_VOCABULARIES + "_" + numIndexes + ".dat",
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
                                    (int) numPostings,
                                    (int) numPostings)
                            );

                    // Write the Posting List in the inverted index files
                    ArrayList<Posting> postings = entry.getPostings();
                    for (Posting posting : postings) {
                        docsBuffer.putInt(posting.getDocumentId());
                        freqsBuffer.putInt(posting.getFrequency());
                    }

                    // Update the statistics of the Vocabulary Entry, BM25Dl, BM25Tf
                    vocEntry.updateStatistics(entry);
                    vocEntry.updateBM25Statistics(entry.getBM25Tf(), entry.getBM25Dl());
                    vocEntry.updateMemoryIdSize((int) numPostings);

                    vocOffset = vocEntry.writeEntry(vocOffset, vocabularyFchan);

                    if(debugMode){
                        entry.debugSaveToDisk("partialDOCIDS_" + numIndexes + ".txt", "partialFREQS_" + numIndexes + ".txt", (int) numPostings);
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

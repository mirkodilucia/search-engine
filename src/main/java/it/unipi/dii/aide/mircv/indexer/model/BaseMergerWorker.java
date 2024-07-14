package it.unipi.dii.aide.mircv.indexer.model;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.BaseVocabularyEntry;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import it.unipi.dii.aide.mircv.utils.FileHandler;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class BaseMergerWorker {

    protected static String PATH_TO_PARTIAL_VOCABULARIES = "data/vocabulary/vocabulary";
    protected static String PATH_TO_PARTIAL_INDEXES_DOCS = "data/indexes/partial_index_docs_";
    protected static String PATH_TO_PARTIAL_INDEXES_FREQS = "data/indexes/partial_index_freqs_";

    private final Config config;

    protected long numIndexes;

    public FileChannel[] documentsIdChannels;
    public FileChannel[] frequencyChannels;

    private static void setupPath(Config config) {
        PATH_TO_PARTIAL_VOCABULARIES = config.getPartialVocabularyPath();
        PATH_TO_PARTIAL_INDEXES_DOCS = config.getPartialIndexesDocumentsPath();
        PATH_TO_PARTIAL_INDEXES_FREQS = config.getPartialIndexesFrequenciesPath();
    }

    protected BaseMergerWorker(Config configuration, int numIndexes) {
        config = configuration;
        setupPath(config);

        this.numIndexes = numIndexes;

        this.documentsIdChannels = new FileChannel[numIndexes];
        this.frequencyChannels = new FileChannel[numIndexes];
    }

    private MappedByteBuffer loadDocumentsIdChannels(VocabularyEntry term, int index) throws IOException {
        BaseVocabularyEntry.VocabularyMemoryInfo memoryInfo = term.getMemoryInfo();
        return documentsIdChannels[index].map(
                FileChannel.MapMode.READ_ONLY,
                memoryInfo.getDocumentIdOffset(),
                memoryInfo.getDocumentIdSize()
        );
    }


    private MappedByteBuffer loadFrequencyChannels(VocabularyEntry term, int index) throws IOException {
        BaseVocabularyEntry.VocabularyMemoryInfo memoryInfo = term.getMemoryInfo();
        return frequencyChannels[index].map(
                FileChannel.MapMode.READ_ONLY,
                memoryInfo.getFrequencyOffset(),
                memoryInfo.getFrequencySize()
        );
    }


    protected PostingList loadList(VocabularyEntry vocabularyEntry, int index) throws IOException {
        MappedByteBuffer docBuffer = loadDocumentsIdChannels(vocabularyEntry, index);
        MappedByteBuffer freqBuffer = loadFrequencyChannels(vocabularyEntry, index);

        PostingList postingList = new PostingList(config, vocabularyEntry.getTerm());

        for (int i = 0; i < vocabularyEntry.getDocumentFrequency(); i++) {
            Posting posting = new Posting(docBuffer.getInt(), freqBuffer.getInt());
            postingList.getPostings().add(posting);
        }

        return postingList;
    }

    protected void cleanup() {
        try{
            for(int i = 0; i < this.numIndexes; i++){
                if(documentsIdChannels[i] != null){
                    documentsIdChannels[i].close();
                }
                if (frequencyChannels[i] != null) {
                    frequencyChannels[i].close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unset(Config config) {
        setupPath(config);

        // Delete PATH_TO_PARTIAL_VOCABULARIES, PATH_TO_PARTIAL_INDEXES_DOCS, PATH_TO_PARTIAL_INDEXES_FREQS
        FileHandler.deleteFile(PATH_TO_PARTIAL_VOCABULARIES);
        FileHandler.deleteFile(PATH_TO_PARTIAL_INDEXES_FREQS);
        FileHandler.deleteFile(PATH_TO_PARTIAL_INDEXES_DOCS);

    }
}

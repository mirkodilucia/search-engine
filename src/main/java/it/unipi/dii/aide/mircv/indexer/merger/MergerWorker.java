package it.unipi.dii.aide.mircv.indexer.merger;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.indexer.model.BaseMergerWorker;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MergerWorker extends BaseMergerWorker {

    private Config config;
    private int numIndexes;

    private VocabularyEntry[] nextTerm;
    private long[] vocabularyEntryMemOffset;

    private void setupPath(Config config) {
        PATH_TO_PARTIAL_VOCABULARIES = config.getPartialVocabularyPath();
        PATH_TO_PARTIAL_INDEXES_DOCS = config.getPartialIndexesDocumentsPath();
        PATH_TO_PARTIAL_INDEXES_FREQS = config.getPartialIndexesFrequenciesPath();
    }

    private MergerWorker(Config config, int numIndexes) {
        super(config, numIndexes);
        setupPath(config);
        this.config = config;
        this.numIndexes = numIndexes;

        this.nextTerm = new VocabularyEntry[numIndexes];
        this.vocabularyEntryMemOffset = new long[numIndexes];

        if(!initialize())
            throw new RuntimeException("Error while initializing MergerWorker");
    }

    public boolean initialize() {
        try {
            for (int i = 0; i < numIndexes; i++) {
                nextTerm[i] = new VocabularyEntry();
                vocabularyEntryMemOffset[i] = 0;

                // read first entry of the vocabulary
                long ret = nextTerm[i].readVocabularyFromDisk(vocabularyEntryMemOffset[i], PATH_TO_PARTIAL_VOCABULARIES + "_" + i + ".dat");

                if (ret == -1 || ret == 0) {
                    nextTerm[i] = null;
                }

                documentsIdChannels[i] = (FileChannel) Files.newByteChannel(Paths.get(PATH_TO_PARTIAL_INDEXES_DOCS + "_" + i + ".dat"),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE
                );

                frequencyChannels[i] = (FileChannel) Files.newByteChannel(Paths.get(PATH_TO_PARTIAL_INDEXES_FREQS + "_" + i + ".dat"),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE
                );
            }
            return true;
        }catch(Exception e){
            cleanup();
            e.printStackTrace();
            return false;
        }
    }

    public static MergerWorker with(Config config, int numIndexes) {
        return new MergerWorker(config, numIndexes);
    }

    public String getMinimumTerm() {
        String term = null;

        for (int i = 0; i < numIndexes; i++) {
            if (nextTerm[i] == null) {
                continue;
            }

            String currentTerm = nextTerm[i].getTerm();
            if (term == null) {
                term = currentTerm;
                continue;
            }

            if (currentTerm.compareTo(term) < 0) {
                term = currentTerm;
            }
        }

        return term;
    }

    public PostingList processTerm(VocabularyEntry vocabularyEntry, String termToProcess,
                                   long documentMemoryOffset, long frequenciesMemoryOffset
    ) throws IOException {
        PostingList finalList = new PostingList(config, termToProcess);

        for (int i=0; i < numIndexes; i++) {
            if (nextTerm[i] == null) {
                continue;
            }

            if (nextTerm[i].getTerm().equals(termToProcess)) {
                PostingList intermediatePostingList = loadList(nextTerm[i], i);
                if (intermediatePostingList == null) {
                    return null;
                }

                vocabularyEntry.updateBM25Statistics(nextTerm[i].getBM25Tf(), nextTerm[i].getBM25Dl());
                vocabularyEntry.updateStatistics(intermediatePostingList);
                finalList.append(intermediatePostingList.getPostings());
            }
        }

        moveToNextTerm(termToProcess);
        vocabularyEntry.computeIDF();
        vocabularyEntry.update(documentMemoryOffset, frequenciesMemoryOffset);

        return finalList;
    }

    private void moveToNextTerm(String termToProcess) {
        for (int i=0; i<numIndexes; i++) {
            if (nextTerm[i] == null) {
                continue;
            }

            if (!nextTerm[i].getTerm().equals(termToProcess)) {
                continue;
            }

            vocabularyEntryMemOffset[i] += VocabularyEntry.ENTRY_SIZE;
            long ret = nextTerm[i].readVocabularyFromDisk(vocabularyEntryMemOffset[i], PATH_TO_PARTIAL_VOCABULARIES+ "_" + i + ".dat");

            if (ret == -1 || ret == 0) {
                nextTerm[i] = null;
            }
        }
    }
}

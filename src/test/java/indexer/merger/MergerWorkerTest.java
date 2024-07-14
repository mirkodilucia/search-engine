package indexer.merger;

import it.unipi.dii.aide.mircv.config.*;
import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.indexer.merger.MergerWorker;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import org.junit.jupiter.api.Test;

public class MergerWorkerTest {

    public Config setupGetMinimumTerm(int i) {
        Config config = new Config(
                "data_test/mergerWorkerTest/documentIndex",
                "data_test/mergerWorkerTest/testDir_" + i,
                "data_test/mergerWorkerTest/debugDir_" + i,
                true
        );
        config.setVocabularyPath(
                new VocabularyConfig(
                "data_test/mergerWorkerTest/vocabulary_" + i + ".dat",
                        "data_test/mergerWorkerTest/documentIndexState_" + i + ".dat"
                ))
                .setBlockDescriptorPath(
                        new BlockDescriptorConfig(
                                "data_test/mergerWorkerTest/block_descriptors_" + i + ".dat", false
                        ))
                .setPartialResultConfig(new PartialResultsConfig(
                    "data_test/mergerWorkerTest/partial_results_" + i,
                    "data_test/mergerWorkerTest/partial_results_" + i,
                    "data_test/mergerWorkerTest/partial_results_" + i
                )).setPartialIndexConfig(new InvertedIndexConfig(
                "data_test/mergerWorkerTest/indexes_docs_" + i,
                "data_test/mergerWorkerTest/indexes_freqs_" + i));

        return config;
    }

    @Test
    public void testGetMinimumTerm() {
        Config config = setupGetMinimumTerm(0);

        long offset = 0;
        Vocabulary.with(config).unset();
        Vocabulary vocabulary = Vocabulary.with(config);

        VocabularyEntry[] nextTerms = new VocabularyEntry[1];
        nextTerms[0] = new VocabularyEntry("term");
        int numIndexes = 1;

        nextTerms[0].writeEntry(offset, vocabulary.getVocabularyChannel());

        vocabulary.put("term", nextTerms[0]);

        DocumentIndexState.updateVocabularySize(vocabulary.size());

        MergerWorker mergerWorker = MergerWorker.with(config, numIndexes);
        String result = mergerWorker.getMinimumTerm();

        assert result.equals("term");
    }

    @Test
    public void testGetMinimumTermWithMultipleTerm() {
        Config config = setupGetMinimumTerm(1);
        long offset = 0;

        Vocabulary vocabulary = Vocabulary.with(config);

        VocabularyEntry[] nextTerms = new VocabularyEntry[2];
        nextTerms[0] = new VocabularyEntry("Apple");
        nextTerms[1] = new VocabularyEntry("apple");
        int numIndexes = 1;
        MergerWorker mergerWorker = MergerWorker.with(config, numIndexes);

        nextTerms[0].writeEntry(offset, vocabulary.getVocabularyChannel());
        offset += VocabularyEntry.ENTRY_SIZE;

        nextTerms[1].writeEntry(offset, vocabulary.getVocabularyChannel());

        vocabulary.put("Apple", nextTerms[0]);
        vocabulary.put("apple", nextTerms[1]);

        //for (int i = 0; i < numIndexes; i++) {
        //    mergerWorker.processTerm(nextTerms[i], nextTerms[i].getTerm(), 0, 0);
        //}

        String result = mergerWorker.getMinimumTerm();
        assert result.equals("Apple");
    }


}

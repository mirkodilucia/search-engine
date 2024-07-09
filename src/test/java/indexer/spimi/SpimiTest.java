package indexer.spimi;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.config.InvertedIndexConfig;
import it.unipi.dii.aide.mircv.config.VocabularyConfig;
import it.unipi.dii.aide.mircv.document.preprocess.FinalDocument;
import it.unipi.dii.aide.mircv.document.table.DocumentIndexEntry;
import it.unipi.dii.aide.mircv.document.table.DocumentIndexTable;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;

import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class SpimiTest {

    private static HashMap<String, PostingList> index = new LinkedHashMap<>();
    private static ArrayList<FinalDocument> testDocuments = new ArrayList<>();

    private static DocumentIndexTable documentIndex;
    private static Vocabulary vocabulary;
    private static Config config;
    private static SpimiMock spimiMock;

    @BeforeAll
    public static void init() {
        config = new Config();
        config.setVocabularyPath(new VocabularyConfig(
                "data_test/spimi/vocabulary_0.dat",
                "data_test/spimi/documentIndexState"
                ))
                .setPartialIndexConfig(new InvertedIndexConfig(
                "data_test/mergerWorkerTest/indexes_docs",
                "data_test/mergerWorkerTest/indexes_freqs"));

        documentIndex = DocumentIndexTable.with(config);
        vocabulary = Vocabulary.with(config);
        spimiMock = SpimiMock.with(config);

        FinalDocument d1 = new FinalDocument("document1", new String[]{"fruit", "apricot", "apple", "fruit", "salad"});
        testDocuments.add(d1);

        FinalDocument d2 = new FinalDocument("document2", new String[]{"apple", "adam", "eve"});
        testDocuments.add(d2);

        PostingList pl = new PostingList(config, "adam\t1:1");
        pl.setStats(5,1);

        PostingList pl1 = new PostingList(config, "apple\t0:1 1:1");
        pl1.setStats(5,1);

        PostingList pl2 = new PostingList(config, "apricot\t0:1");
        pl2.setStats(5,1);

        PostingList pl3 = new PostingList(config, "eve\t1:1");
        pl3.setStats(3,1);

        PostingList pl4 = new PostingList(config, "fruit\t0:2");
        pl4.setStats(5,2);

        PostingList pl5 = new PostingList(config, "salad\t0:1");
        pl.setStats(5,1);

        index.put("adam", pl);
        index.put("apple", pl1);
        index.put("apricot", pl2);
        index.put("eve", pl3);
        index.put("fruit", pl4);
        index.put("salad", pl5);

        documentIndex.put(0, new DocumentIndexEntry(config, "document1",0,5));
        documentIndex.put(1, new DocumentIndexEntry(config, "document2",1,3));

        VocabularyEntry e = new VocabularyEntry("adam");
        e.setDocumentFrequency(1);
        e.setDocumentIdOffset(0);
        e.setFrequencyOffset(0);
        e.setMaxTermFrequency(1);
        e.setBM25Dl(5);
        e.updateMemoryIdSize(4);

        VocabularyEntry e1 = new VocabularyEntry("apple");
        e.setDocumentFrequency(1);
        e.setDocumentIdOffset(140);
        e.setFrequencyOffset(140);
        e.setMaxTermFrequency(1);
        e.setBM25Dl(5);
        e.updateMemoryIdSize(4);

        VocabularyEntry e2 = new VocabularyEntry("apricot");
        e.setDocumentFrequency(1);
        e.setDocumentIdOffset(280);
        e.setFrequencyOffset(280);
        e.setMaxTermFrequency(1);
        e.setBM25Dl(5);
        e.updateMemoryIdSize(8);

        VocabularyEntry e3 = new VocabularyEntry("eve");
        e.setDocumentFrequency(1);
        e.setDocumentIdOffset(420);
        e.setFrequencyOffset(420);
        e.setMaxTermFrequency(1);
        e.setBM25Dl(3);
        e.updateMemoryIdSize(4);

        VocabularyEntry e4 = new VocabularyEntry("fruit");
        e.setDocumentFrequency(1);
        e.setDocumentIdOffset(560);
        e.setFrequencyOffset(560);
        e.setMaxTermFrequency(2);
        e.setBM25Dl(5);
        e.updateMemoryIdSize(4);

        VocabularyEntry e5 = new VocabularyEntry("salad");
        e.setDocumentFrequency(1);
        e.setDocumentIdOffset(700);
        e.setFrequencyOffset(700);
        e.setMaxTermFrequency(1);
        e.setBM25Dl(5);
        e.updateMemoryIdSize(4);

        vocabulary.put("adam", e);
        vocabulary.put("apple", e1);
        vocabulary.put("apricot", e2);
        vocabulary.put("eve", e3);
        vocabulary.put("fruit", e4);
        vocabulary.put("salad", e5);
    }

    @Test
    public void buildVocabulary_ShouldbeEqual() {
        assertEquals(vocabulary, spimiMock.buildVocabulary(index));
    }

    @Test
    public void buildDocumentIndex_ShouldBeEqual() {
        DocumentIndexTable testDocumentIndex = SpimiMock.with(config).buildDocumentIndexTable(testDocuments);
        assertEquals(documentIndex, testDocumentIndex);
    }

    @Test
    public void buildVocabulary_ShouldBeEqual() {
        Vocabulary testVocabulary = SpimiMock.with(config).buildVocabulary(index);
        assertEquals(vocabulary, testVocabulary);
    }

    @Test
    public void buildIndex_ShouldBeEqual() {
        DocumentIndexTable testDocumentIndex = SpimiMock.with(config).buildDocumentIndexTable(testDocuments);
        Vocabulary testVocabulary = SpimiMock.with(config).buildVocabulary(index);
        assertEquals(documentIndex, testDocumentIndex);
        assertEquals(vocabulary, testVocabulary);

        String result = SpimiMock.with(config).executeSpimiInMemory(testDocuments).toString();
        String indexString = index.toString();

        assertEquals(indexString, result);
    }

    @AfterAll
    static void teardown() {
        //FileHandler.removeFile("test/data/testDocumentDocids");
        //FileHandler.removeFile("test/data/testDocumentFreqs");
    }

    /*
    @AfterEach
    void cleanup() {
        documentIndex.dat = null;
        vocabulary = null;
        config = null;
        index = null;
        testDocuments = null;


    }
    */
}

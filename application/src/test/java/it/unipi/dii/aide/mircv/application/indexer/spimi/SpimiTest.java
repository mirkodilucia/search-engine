package it.unipi.dii.aide.mircv.application.indexer.spimi;

import it.unipi.dii.aide.mircv.application.data.*;
import it.unipi.dii.aide.mircv.application.utils.FileUtils;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;

import it.unipi.dii.aide.mircv.application.config.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;

public class SpimiTest {

    private static HashMap<String, PostingList> index = new LinkedHashMap<>();
    private static ArrayList<FinalDocument> testDocuments = new ArrayList<>();

    private static DocumentIndexTable documentIndex;
    private static Vocabulary vocabulary;
    private static Config config;

    private void init() {
        config = new Config();
        config.setDocumentIdFolder("../test/data/spimi/");
        config.setDocumetIdsFileName("documentIndex");
        config.setPartialIndexesPath("../test/data/spimi/");
        config.setDocumentIndexPath("../test/data/spimi/documentIndex");
        config.setDocumentFreqPath("../test/data/spimi/", "testDocumentFreqs_0");

        documentIndex = DocumentIndexTable.with(config);
        vocabulary = Vocabulary.with(config);

        FinalDocument d1 = new FinalDocument();
        d1.setPid("document1");
        d1.setTokens(new String[]{"fruit", "apricot", "apple", "fruit", "salad"});
        testDocuments.add(d1);

        FinalDocument d2 = new FinalDocument();
        d2.setPid("document2");
        d2.setTokens(new String[]{"apple", "adam", "eve"});
        testDocuments.add(d2);

        PostingList pl = new PostingList(config, "adam\t1:1");
        pl.setBM25Dl(5);
        pl.setBM25Tf(1);

        PostingList pl1 = new PostingList(config, "apple\t0:1 1:1");
        pl.setBM25Dl(5);
        pl.setBM25Tf(1);

        PostingList pl2 = new PostingList(config, "apricot\t0:1");
        pl.setBM25Dl(5);
        pl.setBM25Tf(1);

        PostingList pl3 = new PostingList(config, "eve\t1:1");
        pl.setBM25Dl(3);
        pl.setBM25Tf(1);

        PostingList pl4 = new PostingList(config, "fruit\t0:2");
        pl.setBM25Dl(5);
        pl.setBM25Tf(2);

        PostingList pl5 = new PostingList(config, "salad\t0:1");
        pl.setBM25Dl(5);
        pl.setBM25Tf(1);

        index.put("adam", pl);
        index.put("apple", pl1);
        index.put("apricot", pl2);
        index.put("eve", pl3);
        index.put("fruit", pl4);
        index.put("salad", pl5);

        documentIndex.put(0,new DocumentIndexEntry(config, "document1",0,5));
        documentIndex.put(1,new DocumentIndexEntry(config, "document2",1,3));

        VocabularyEntry e = new VocabularyEntry("adam");
        e.setDocumentFrequency(1);
        e.setDocIdOffset(0);
        e.setFrequencyOffset(0);
        e.setMaxTermFrequency(1);
        e.setBM25Dl(5);
        e.setDocIdSize(4);
        e.setFrequencySize(4);

        VocabularyEntry e1 = new VocabularyEntry("apple");
        e.setDocumentFrequency(1);
        e.setDocIdOffset(140);
        e.setFrequencyOffset(140);
        e.setMaxTermFrequency(1);
        e.setBM25Dl(5);
        e.setDocIdSize(4);
        e.setFrequencySize(4);

        VocabularyEntry e2 = new VocabularyEntry("apricot");
        e.setDocumentFrequency(1);
        e.setDocIdOffset(280);
        e.setFrequencyOffset(280);
        e.setMaxTermFrequency(1);
        e.setBM25Dl(5);
        e.setDocIdSize(8);
        e.setFrequencySize(8);

        VocabularyEntry e3 = new VocabularyEntry("eve");
        e.setDocumentFrequency(1);
        e.setDocIdOffset(420);
        e.setFrequencyOffset(420);
        e.setMaxTermFrequency(1);
        e.setBM25Dl(3);
        e.setDocIdSize(4);
        e.setFrequencySize(4);

        VocabularyEntry e4 = new VocabularyEntry("fruit");
        e.setDocumentFrequency(1);
        e.setDocIdOffset(560);
        e.setFrequencyOffset(560);
        e.setMaxTermFrequency(2);
        e.setBM25Dl(5);
        e.setDocIdSize(4);
        e.setFrequencySize(4);

        VocabularyEntry e5 = new VocabularyEntry("salad");
        e.setDocumentFrequency(1);
        e.setDocIdOffset(700);
        e.setFrequencyOffset(700);
        e.setMaxTermFrequency(1);
        e.setBM25Dl(5);
        e.setDocIdSize(4);
        e.setFrequencySize(4);


        vocabulary.put("adam",e);
        vocabulary.put("apple",e1);
        vocabulary.put("apricot",e2);
        vocabulary.put("eve",e3);
        vocabulary.put("fruit",e4);
        vocabulary.put("salad",e5);
    }

    @Test
    public void buildDocumentIndex_ShouldBeEqual() {
        init();
        DocumentIndexTable testDocumentIndex = SpimiMock.with(config).buildDocumentIndexTable(testDocuments);
        assertEquals(documentIndex, testDocumentIndex);
    }

    @Test
    public void buildVocabulary_ShouldBeEqual() {
        init();
        Vocabulary testVocabulary = SpimiMock.with(config).buildVocabulary(index);
        assertEquals(vocabulary, testVocabulary);
    }

    @Test
    public void buildIndex_ShouldBeEqual() {
        init();
        DocumentIndexTable testDocumentIndex = SpimiMock.with(config).buildDocumentIndexTable(testDocuments);
        Vocabulary testVocabulary = SpimiMock.with(config).buildVocabulary(index);
        assertEquals(documentIndex, testDocumentIndex);
        assertEquals(vocabulary, testVocabulary);

        String result = SpimiMock.with(config).executeSpimiInMemory(testDocuments).toString();
        assertEquals(index.toString(), result);
    }

    @AfterEach
    void cleanup() {
        documentIndex = null;
        vocabulary = null;
        config = null;
        index = null;
        testDocuments = null;

        FileUtils.removeFile(config.getDocumentIndexPath());
        FileUtils.removeFile(config.getFrequencyFolder() + config.getPartialIndexFreqsPath(0));
    }
}

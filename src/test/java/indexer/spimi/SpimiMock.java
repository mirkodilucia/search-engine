package indexer.spimi;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.document.preprocess.FinalDocument;
import it.unipi.dii.aide.mircv.document.table.DocumentIndexEntry;
import it.unipi.dii.aide.mircv.document.table.DocumentIndexTable;
import it.unipi.dii.aide.mircv.indexer.spimi.Spimi;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.indexer.model.Posting;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;

import java.io.IOException;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

public class SpimiMock extends Spimi {

    protected SpimiMock(Config config) {
        super(config);
    }

    public DocumentIndexTable buildDocumentIndexTable(ArrayList<FinalDocument> testDocuments) {
        DocumentIndexTable documentIndexTable = DocumentIndexTable.with(config);

        int docCounter =0;
        for (FinalDocument doc : testDocuments) {
            DocumentIndexEntry entry = new DocumentIndexEntry(config, doc.getDocId(), docCounter, doc.getTokens().size());
            documentIndexTable.put(docCounter, entry);
            docCounter++;

        }

        return documentIndexTable;
    }

    public static SpimiMock with(Config config) {
        return new SpimiMock(config);
    }

    public Vocabulary buildVocabulary(HashMap<String, PostingList> index) {
        Files.exists(Paths.get("test/data"));

        Vocabulary vocabulary = Vocabulary.with(config);
        try (
                FileChannel docsFchan = FileChannelHandler.open(config.invertedIndexConfig.getPartialIndexDocumentsPath(0),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE
                );
                FileChannel freqsFchan = FileChannelHandler.open(config.invertedIndexConfig.getPartialIndexFrequenciesPath(0),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
        ) {

            int countPostings = 0;
            for (PostingList postingList : index.values()) {
                countPostings += postingList.getPostings().size();
            }

            MappedByteBuffer docsMbb = docsFchan.map(FileChannel.MapMode.READ_WRITE, 0, countPostings * 4L);
            MappedByteBuffer freqsMbb = freqsFchan.map(FileChannel.MapMode.READ_WRITE, 0, countPostings * 4L);

            if(docsMbb != null && freqsMbb != null) {
                for(PostingList postingList : index.values()) {

                    VocabularyEntry vocabularyEntry = new VocabularyEntry(postingList.getTerm());
                    vocabularyEntry.setDocumentIdOffset(docsMbb.position());
                    vocabularyEntry.setFrequencyOffset(freqsMbb.position());

                    for(Posting posting : postingList.getPostings()) {
                        docsMbb.putInt(posting.getDocumentId());
                        freqsMbb.putInt(posting.getFrequency());
                    }

                    vocabularyEntry.updateMemoryIdSize((countPostings*4));
                    vocabularyEntry.updateStatistics(postingList);
                    vocabularyEntry.setBM25Tf(postingList.getBM25Tf());
                }
            }
            return vocabulary;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public  HashMap<String, PostingList> executeSpimiInMemory(ArrayList<FinalDocument> testDocuments) {
        HashMap<String, PostingList> index = new HashMap<>();
        int docId = 0;
        int documentLength = 0;

        for (FinalDocument doc : testDocuments) {
            for (String token : doc.getTokens())
            {
                PostingList posting;
                if (!index.containsKey(token)) {
                    posting = new PostingList(config, token);
                    index.put(token, posting);
                }
                else {
                    posting = index.get(token);
                }
                documentLength += doc.getTokens().size();

                updateOrAddPosting(docId, posting);
                posting.updateBM25Parameters(documentLength, posting.getPostings().size());
                posting.debugSaveToDisk("docSpimiMock", "freqSpimiMock", posting.getPostings().size());
            }
            docId++;
        }
        index = index.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return index;
    }


}
package it.unipi.dii.aide.mircv.application.indexer.spimi;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.*;
import it.unipi.dii.aide.mircv.application.indexer.FileChannelUtils;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SpimiMock extends Spimi {

    protected SpimiMock(Config config) {
        super(config);
    }

    public DocumentIndexTable buildDocumentIndexTable(ArrayList<FinalDocument> testDocuments) {
           DocumentIndexTable documentIndexTable = DocumentIndexTable.with(config);

           int docCounter =0;
           for (FinalDocument doc : testDocuments) {
               DocumentIndexEntry entry = new DocumentIndexEntry(config, doc.getPid(), docCounter, doc.getTokens().size());
               documentIndexTable.put(docCounter, entry);
               docCounter++;

           }

        return documentIndexTable;
    }

    public static SpimiMock with(Config config) {
        return new SpimiMock(config);
    }

    public Vocabulary buildVocabulary(HashMap<String, PostingList> index) {
        try (
                FileChannel docsFchan = FileChannelUtils.openFileChannel(config.getPartialIndexDocsPath(0),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE
                );
                FileChannel freqsFchan = FileChannelUtils.openFileChannel(config.getPartialIndexFreqsPath(0),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
        ) {
            Vocabulary vocabulary = Vocabulary.with(config.getPartialVocabularyPath(0));

            int countPostings = 0;
            for (PostingList postingList : index.values()) {
                countPostings += postingList.getPostings().size();
            }

            MappedByteBuffer docsMbb = docsFchan.map(FileChannel.MapMode.READ_WRITE, 0, countPostings * 4L);

            MappedByteBuffer freqsMbb = freqsFchan.map(FileChannel.MapMode.READ_WRITE, 0, countPostings * 4L);

            if(docsMbb != null && freqsMbb != null) {
                for(PostingList postingList : index.values()) {

                    VocabularyEntry vocabularyEntry = new VocabularyEntry(postingList.getTerm(), config.getPartialVocabularyPath(0));
                    vocabularyEntry.setDocidOffset(docsMbb.position());
                    vocabularyEntry.setFrequencyOffset(freqsMbb.position());

                    for(Posting posting : postingList.getPostings()) {
                        docsMbb.putInt(posting.getDocId());
                        freqsMbb.putInt(posting.getFrequency());
                    }

                    vocabularyEntry.setDocIdSize((int) (countPostings*4));
                    vocabularyEntry.setFrequencySize((int) (countPostings*4));
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
                // create new posting list if term wasn't present yet
                if (!index.containsKey(token)) {
                    posting = new PostingList(config, token);
                    index.put(token, posting);
                }
                else {
                    //if already present get the posting list
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

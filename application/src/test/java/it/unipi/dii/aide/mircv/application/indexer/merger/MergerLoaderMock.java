package it.unipi.dii.aide.mircv.application.indexer.merger;

import it.unipi.dii.aide.mircv.application.compression.UnaryCompressor;
import it.unipi.dii.aide.mircv.application.compression.VariableByteCompressor;
import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.indexer.FileChannelUtils;
import it.unipi.dii.aide.mircv.application.indexer.MergerLoader;

import it.unipi.dii.aide.mircv.application.data.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class MergerLoaderMock {

    private static HashMap<String, PostingList> index = new LinkedHashMap<>();
    private static ArrayList<InitialDocument> testDocuments = new ArrayList<>();
    private static DocumentIndexTable documentIndex;
    private static Vocabulary vocabulary;

    public static MergerLoader load(Config config) {
        documentIndex = DocumentIndexTable.with(config);
        vocabulary = Vocabulary.with(config);

        InitialDocument d1 = new InitialDocument(config, "document1", "fruit apricot apple fruit salad");
        ArrayList<String> tokens1 = new ArrayList<>(Arrays.asList("fruit", "apricot", "apple", "fruit", "salad"));
        d1.setTokens(tokens1);
        testDocuments.add(d1);

        InitialDocument d2 = new InitialDocument(config, "document2", "apple adam eve");
        ArrayList<String> tokens2 = new ArrayList<>(Arrays.asList("apple", "adam", "eve"));
        d2.setTokens(tokens2);
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

        documentIndex.put(0, new DocumentIndexEntry(config, "document1", 0, 5));
        documentIndex.put(1, new DocumentIndexEntry(config, "document2", 1, 3));

        VocabularyEntry vocabularyEntry1 = new VocabularyEntry("adam", 1, 0, 0, 1, 5, 4, 4);
        VocabularyEntry e1 = new VocabularyEntry("apple", 1, 140, 140, 1, 5, 4, 4);
        VocabularyEntry e2 = new VocabularyEntry("apricot", 1, 280, 280, 1, 5, 4, 4);
        VocabularyEntry e3 = new VocabularyEntry("eve", 1, 420, 420, 1, 3, 4, 4);
        VocabularyEntry e4 = new VocabularyEntry("fruit", 1, 560, 560, 2, 5, 4, 4);
        VocabularyEntry e5 = new VocabularyEntry("salad", 1, 700, 700, 1, 5, 4, 4);

        vocabulary.put("adam", vocabularyEntry1);
        vocabulary.put("apple", e1);
        vocabulary.put("apricot", e2);
        vocabulary.put("eve", e3);
        vocabulary.put("fruit", e4);
        vocabulary.put("salad", e5);

        try (
                FileChannel documentIdChannel =
                        FileChannelUtils.openFileChannel(config.getInvertedIndexConfig().getInvertedIndexDocs(),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE);
                FileChannel frequencyChan =
                        FileChannelUtils.openFileChannel(config.getInvertedIndexConfig().getInvertedIndexFreqsFile(),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE);
                FileChannel descriptorChan =
                        FileChannelUtils.openFileChannel(config.getBlockDescriptorConfig().getBlockDescriptorsPath(),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE);
        ) {
            BlockDescriptor blockDescriptor = new BlockDescriptor();

            vocabularyEntry1.computeBlocksInformation();
            int maxNumPostings = vocabularyEntry1.getMaxNumberOfPostingsInBlock();

            FileChannel[] documentsId = new FileChannel[1];
            documentsId[0] = documentIdChannel;

            FileChannel[] frequencyChannels = new FileChannel[1];
            frequencyChannels[0] = frequencyChan;

            int[] docids = new int[1];
            int[] freqs = new int[1];

            MergerLoader loader = new MergerLoader(config, 1);

            Iterator<Posting> plIterator = pl.getPostings().iterator();
            while (plIterator.hasNext()) {
                Posting currPosting = plIterator.next();
                byte[] compressedDocs = VariableByteCompressor.integerArrayCompression(docids);
                byte[] compressedFreqs = UnaryCompressor.integerArrayCompression(freqs);

                loader.writeCompressedPostingListsToDisk(currPosting, documentsId[0], frequencyChannels[0], descriptorChan,
                        compressedDocs, compressedFreqs, blockDescriptor, 1 * 4L, 1 * 4L, maxNumPostings);

            }
            return loader;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}

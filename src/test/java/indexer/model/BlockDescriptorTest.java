package indexer.model;

import it.unipi.dii.aide.mircv.config.*;
import it.unipi.dii.aide.mircv.indexer.merger.MergerFileChannel;
import it.unipi.dii.aide.mircv.indexer.model.BlockDescriptor;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.indexer.model.Posting;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;


public class BlockDescriptorTest {

    private static final String vocabularyPath = "data_test/blockDescriptorsTest/vocabulary_0.dat";
    private static final String blockDescriptorPath = "data_test/blockDescriptorsTest/block_descriptor.dat";

    static Config config;
    private static Vocabulary vocabulary;

    @BeforeAll
    static void setTestPaths() {
        config = new Config();
        config.setVocabularyPath(new VocabularyConfig(
                vocabularyPath,
                "data_test/blockDescriptorsTest/documentIndexState"
        )).setBlockDescriptorPath(new BlockDescriptorConfig(
                blockDescriptorPath,
                false
        )).setPartialResultConfig(new PartialResultsConfig(
                "data_test/blockDescriptorsTest/partial_results",
                "data_test/blockDescriptorsTest/partial_results",
                "data_test/blockDescriptorsTest/partial_results"
        )).setPartialIndexConfig(new InvertedIndexConfig(
                "data_test/blockDescriptorsTest/indexes_docs",
                "data_test/blockDescriptorsTest/indexes_freqs"));

        vocabulary = Vocabulary.with(config);
    }

    @BeforeEach
    void reset() {
        try {
            Files.deleteIfExists(Paths.get(vocabularyPath));
            Files.deleteIfExists(Paths.get(blockDescriptorPath));
            vocabulary.reset();
            BlockDescriptor.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    void oneDescriptorBlockTest() {
        // create a posting list with 1023 elements
        PostingList list = new PostingList(config, "test");
        for (int i = 0; i < 1023; i++) {
            Posting posting = new Posting(i, ThreadLocalRandom.current().nextInt(1, 101));
            list.getPostings().add(posting);
        }
        // update block information
        VocabularyEntry voc = new VocabularyEntry("test");
        voc.updateStatistics(list);
        voc.computeBlockInformation();

        // check the number of blocks
        assertEquals(1, voc.getHowManyBlockToWrite());

        try (
                FileChannel blockChannel = (FileChannel) Files.newByteChannel(
                        Paths.get(blockDescriptorPath),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE)
        ) {

            //voc.writeEntry(0, vocabularyChannel);

            BlockDescriptor blockDescriptor = new BlockDescriptor();
            blockDescriptor.setDocumentIdOffset(0);
            blockDescriptor.setDocumentIdSize(voc.getDocumentFrequency() * 4);

            blockDescriptor.setMaxDocumentsId(list.getPostings().get(voc.getDocumentFrequency() - 1).getDocumentId());

            blockDescriptor.setFrequenciesOffset(0);
            blockDescriptor.setFrequenciesSize(voc.getDocumentFrequency() * 4);
            blockDescriptor.setNumPostings(list.getPostings().size());

            MergerFileChannel.CompressionResult result = blockDescriptor.writeBlock(blockChannel);
            assertTrue(result != null);

            ArrayList<BlockDescriptor> blocks = voc.readBlocks();

            assertEquals(1, blocks.size());

            BlockDescriptor block = blocks.get(0);

            assertEquals(blockDescriptor.getDocumentIdOffset(), block.getDocumentIdOffset());
            assertEquals(blockDescriptor.getFrequenciesOffset(), block.getFrequenciesOffset());

            assertEquals(blockDescriptor.getDocumentIdSize(), block.getDocumentIdSize());
            assertEquals(blockDescriptor.getMaxDocumentsId(), block.getMaxDocumentsId());

            assertEquals(blockDescriptor.getFrequeanciesSize(), block.getFrequeanciesSize());
            assertEquals(blockDescriptor.getNumPostings(), block.getNumPostings());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void multipleDescriptorsTest() {
        vocabulary = Vocabulary.with(config);

        // create a posting list with 1025 elements
        PostingList list = new PostingList(config, "test");
        for (int i = 1; i <= 1025; i++) {
            Posting posting = new Posting(i, ThreadLocalRandom.current().nextInt(1, 101));
            list.getPostings().add(posting);
        }
        // update block information
        VocabularyEntry voc = new VocabularyEntry("test");
        voc.updateStatistics(list);
        voc.computeBlockInformation();

        // check the number of blocks
        assertEquals(33, voc.getHowManyBlockToWrite());

        int numBlocks = voc.getHowManyBlockToWrite();
        int maxNumPostings = voc.getMaxNumberOfPostingInBlock();
        int docsMemOffset = 0;
        int freqsMemOffset = 0;

        try (
                FileChannel blockChannel = (FileChannel) Files.newByteChannel(
                        Paths.get(blockDescriptorPath),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE)
        ) {
            Iterator<Posting> plIterator = list.getPostings().iterator();
            ArrayList<BlockDescriptor> blockList = new ArrayList<>();

            for (int i = 0; i < numBlocks; i++) {
                BlockDescriptor blockDescriptor = new BlockDescriptor();
                blockDescriptor.setDocumentIdOffset(docsMemOffset);
                blockDescriptor.setFrequenciesOffset(freqsMemOffset);

                int postingsInBlock = 0;
                int nPostingsToBeWritten = list.getPostingsToBeWritten(i, maxNumPostings);

                blockDescriptor.setDocumentIdSize(nPostingsToBeWritten * 4);
                blockDescriptor.setFrequenciesSize(nPostingsToBeWritten * 4);

                while (true) {
                    Posting currPosting = plIterator.next();
                    postingsInBlock++;

                    if (postingsInBlock == nPostingsToBeWritten) {
                        blockDescriptor.setMaxDocumentsId(currPosting.getDocumentId());
                        blockDescriptor.setNumPostings(postingsInBlock);
                        blockDescriptor.writeBlock(blockChannel);

                        blockList.add(blockDescriptor);

                        docsMemOffset += nPostingsToBeWritten * 4;
                        freqsMemOffset += nPostingsToBeWritten * 4;
                        break;
                    }
                }
            }

            ArrayList<BlockDescriptor> blocks = voc.readBlocks();
            assertEquals(33, blocks.size());

            for (int i = 0; i < blocks.size(); i++) {
                assertEquals(blockList.get(i).toString(), blocks.get(i).toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
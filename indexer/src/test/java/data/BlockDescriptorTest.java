package data;



import it.unipi.dii.aide.mircv.Posting;
import it.unipi.dii.aide.mircv.PostingList;
import it.unipi.dii.aide.mircv.data.BlockDescriptor;
import it.unipi.dii.aide.mircv.data.VocabularyEntry;
import it.unipi.dii.aide.mircv.utils.FileUtils;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

public class BlockDescriptorTest {

    // Setting up test paths before running the tests
    @BeforeAll
    static void setTestPaths() {
        VocabularyEntry.setBlockDescriptorsPath("src/test/data/blockDescriptorsTest");
    }

    // Test for a single descriptor block
    @Test
    public void oneDescriptorBlockTest() {
        // create a posting list with 1023 elements
        PostingList list = new PostingList("test");
        for (int i = 0; i < 1023; i++) {
            Posting posting = new Posting(i, ThreadLocalRandom.current().nextInt(1, 101));
            list.getPostings().add(posting);
        }
        // update block information
        VocabularyEntry voc = new VocabularyEntry("test");
        voc.updateValues(list);
        voc.computeBlocksInformation();

        // check the number of blocks
        assertEquals(1, voc.getNumBlocks());

        try (
                FileChannel blockChannel = (FileChannel) Files.newByteChannel(
                        Paths.get("src/test/data/blockDescriptorsTest"),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE)
        ) {
            // Creating a block descriptor and setting its values
            BlockDescriptor blockDescriptor = new BlockDescriptor();
            blockDescriptor.setDocidOffset(0);
            blockDescriptor.setDocidSize(voc.getDocumentFrequency() * 4);
            blockDescriptor.setMaxDocid(list.getPostings().get(voc.getDocumentFrequency() - 1).getDocId());
            blockDescriptor.setFreqOffset(0);
            blockDescriptor.setFreqSize(voc.getDocumentFrequency() * 4);
            blockDescriptor.setNumPostings(list.getPostings().size());

            // Save the block descriptor on disk
            assertTrue(blockDescriptor.saveDescriptorOnDisk(blockChannel));

            // Read blocks from the disk
            ArrayList<BlockDescriptor> blocks = voc.readBlocks();
            assertEquals(1, blocks.size());

            // Verify the equality of the saved and read block descriptors
            assertEquals(blockDescriptor, blocks.get(0));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Test for multiple descriptors
    @Test
    public void multipleDescriptorsTest() {
        // create a posting list with 1025 elements
        PostingList list = new PostingList("test");
        for (int i = 1; i <= 1025; i++) {
            Posting posting = new Posting(i, ThreadLocalRandom.current().nextInt(1, 101));
            list.getPostings().add(posting);
        }
        // update block information
        VocabularyEntry voc = new VocabularyEntry("test");
        voc.updateValues(list);
        voc.computeBlocksInformation();

        // check the number of blocks
        assertEquals(33, voc.getNumBlocks());

        // Number of blocks and maximum number of postings in a block
        int numBlocks = voc.getNumBlocks();
        int maxNumPostings = voc.getMaxNumberOfPostingsInBlock();

        // Memory offsets for document IDs and frequencies
        int docsMemOffset = 0;
        int freqsMemOffset = 0;

        // Create iterator over posting list
        Iterator<Posting> plIterator = list.getPostings().iterator();
        try (
                FileChannel descriptorChan = (FileChannel) Files.newByteChannel(
                        Paths.get("src/test/data/blockDescriptorsTest"),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE)
        ) {
            ArrayList<BlockDescriptor> blockList = new ArrayList<>();

            // Simulation of saving the posting lists
            for (int i = 0; i < numBlocks; i++) {
                // Create a new block descriptor and update its information
                BlockDescriptor blockDescriptor = new BlockDescriptor();
                blockDescriptor.setDocidOffset(docsMemOffset);
                blockDescriptor.setFreqOffset(freqsMemOffset);

                // Number of postings written in the block
                int postingsInBlock = 0;
                int alreadyWrittenPostings = i * maxNumPostings;

                // Number of postings to be written in the current block
                int nPostingsToBeWritten = Math.min((list.getPostings().size() - alreadyWrittenPostings), maxNumPostings);

                // Set docs and freqs num bytes as (number of postings) * 4
                blockDescriptor.setDocidSize(nPostingsToBeWritten * 4);
                blockDescriptor.setFreqSize(nPostingsToBeWritten * 4);

                while(true) {
                    // Get next posting to be written to disk
                    Posting currPosting = plIterator.next();

                    // Increment counter of number of postings written in the block
                    postingsInBlock++;

                    // Check if currPosting is the last posting to be written in the current block
                    if (postingsInBlock == nPostingsToBeWritten) {
                        // Update the max docid of the block
                        blockDescriptor.setMaxDocid(currPosting.getDocId());

                        // Update the number of postings in the block
                        blockDescriptor.setNumPostings(postingsInBlock);

                        // Write the block descriptor on disk
                        blockDescriptor.saveDescriptorOnDisk(descriptorChan);

                        blockList.add(blockDescriptor);

                        docsMemOffset += nPostingsToBeWritten * 4L;
                        freqsMemOffset += nPostingsToBeWritten * 4L;

                        break;
                    }
                }
            }

            // Read blocks from the disk
            ArrayList<BlockDescriptor> blocks = voc.readBlocks();
            assertEquals(33, blocks.size());

            // Verify the equality of the saved and read block descriptors
            for (int i = 0; i < blocks.size(); i++) {
                assertEquals(blockList.get(i), blocks.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Removing the file created during tests
    @AfterAll
    static void removeFile() {
        FileUtils.removeFile("src/test/data/blockDescriptorsTest");
    }
}

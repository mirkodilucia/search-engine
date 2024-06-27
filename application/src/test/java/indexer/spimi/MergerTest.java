package indexer.spimi;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class MergerTest {

    @Test
    public void testMergeBlocks() throws IOException, ClassNotFoundException {
        /*
        Config config = new Config();

        // Create first block
        Vocabulary vocab1 = new Vocabulary(config);
        vocab1.addTerm("test", 1);
        BlockDescriptor block1 = new BlockDescriptor(0);
        block1.writeBlock(vocab1);

        // Create second block
        Vocabulary vocab2 = new Vocabulary(config);
        vocab2.addTerm("example", 2);
        BlockDescriptor block2 = new BlockDescriptor(1);
        block2.writeBlock(vocab2);

        // Merge blocks
        // Merger.with(config).mergeBlocks(1);

        // Read final index
        Vocabulary finalIndex = Vocabulary.readVocabulary("final_index.dat");

        assertNotNull(finalIndex);
        Map<String, PostingList> index = finalIndex.getIndex();
        assertTrue(index.containsKey("test"));
        assertTrue(index.containsKey("example"));
        assertTrue(index.get("test").getPostings().contains(1));
        assertTrue(index.get("example").getPostings().contains(2));
        */
    }
}
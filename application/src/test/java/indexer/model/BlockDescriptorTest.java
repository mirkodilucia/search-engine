package indexer.model;

import it.unipi.dii.aide.mircv.indexer.model.BlockDescriptor;
import it.unipi.dii.aide.mircv.indexer.model.Vocabulary;
import org.junit.jupiter.api.Test;

import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

public class BlockDescriptorTest {

    @Test
    public void testWriteReadBlock() throws IOException, ClassNotFoundException {
        /*
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.addTerm("test", 1);

        BlockDescriptor blockDescriptor = new BlockDescriptor(0);
        blockDescriptor.writeBlock(vocabulary);
        Vocabulary loadedVocabulary = blockDescriptor.readBlock();

        assertNotNull(loadedVocabulary);
        assertTrue(loadedVocabulary.getIndex().containsKey("test"));
        assertTrue(loadedVocabulary.getIndex().get("test").getPostings().contains(1));
        */
    }
}
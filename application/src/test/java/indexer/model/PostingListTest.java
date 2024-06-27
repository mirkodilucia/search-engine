package indexer.model;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.indexer.model.Posting;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PostingListTest {

    @Test
    public void testAdd() {
        Config config = new Config();
        PostingList postingList = new PostingList(config);
        postingList.add(1);
        ArrayList<Posting> postings = postingList.getPostings();

        assertEquals(1, postings.size());
        assertEquals(1, postings.get(0).getDocumentId());
    }

    /*
    @Test
    public void testUnaryConversion() {
        PostingList postingList = new PostingList();
        postingList.add(1);
        postingList.add(3);

        String unary = postingList.toUnary();
        PostingList decodedList = PostingList.fromUnary(unary);

        assertTrue(decodedList.getPostings().contains(1));
        assertTrue(decodedList.getPostings().contains(3));
    }
    */
}
package indexer.model;

import it.unipi.dii.aide.mircv.config.model.Config;
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

    @Test
    public void testIsEmpty() {
        Config config = new Config();
        PostingList postingList = new PostingList(config);
        assertTrue(postingList.isEmpty());
    }

    @Test
    public void testParsePostings() {
        Config config = new Config();
        PostingList postingList = new PostingList(config);
        postingList.parsePostings("1:2 3:4");
        ArrayList<Posting> postings = postingList.getPostings();

        assertEquals(2, postings.size());
        assertEquals(1, postings.get(0).getDocumentId());
        assertEquals(2, postings.get(0).getFrequency());
        assertEquals(3, postings.get(1).getDocumentId());
        assertEquals(4, postings.get(1).getFrequency());
    }

    @Test
    public void testPostingList() {
        Config config = new Config();
        PostingList postingList = new PostingList(config, "term\t1:2 3:4");
        ArrayList<Posting> postings = postingList.getPostings();

        assertEquals(2, postings.size());
        assertEquals(1, postings.get(0).getDocumentId());
        assertEquals(2, postings.get(0).getFrequency());
        assertEquals(3, postings.get(1).getDocumentId());
        assertEquals(4, postings.get(1).getFrequency());
    }

    @Test
    public void testPostingListConfig() {
        Config config = new Config();
        PostingList postingList = new PostingList(config);
        assertNotNull(postingList);
    }

    @Test
    public void testUpdateOrAddPosting() {
        Config config = new Config();
        PostingList postingList = new PostingList(config);
        postingList.add(1);
        postingList.updateOrAddPosting(1);
        ArrayList<Posting> postings = postingList.getPostings();
        assertEquals(1, postings.size());
        assertEquals(1, postings.get(0).getDocumentId());
        assertEquals(2, postings.get(0).getFrequency());
    }
}
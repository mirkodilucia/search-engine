package it.unipi.dii.aide.mircv.application.data;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.config.Configuration;
import it.unipi.dii.aide.mircv.application.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A posting list, with its access methods leveraging skipping
 */
public class PostingList {

    private static Config config;

    // Term associated with the posting list
    private String term;

    // List of postings loaded in memory
    private final ArrayList<Posting> postings = new ArrayList<>();

    // List of blocks in which the posting list is divided
    private ArrayList<BlockDescriptor> blocks = null;

    // Iterators for postings and blocks
    private Iterator<Posting> postingIterator = null;
    private Iterator<BlockDescriptor> blocksIterator = null;

    // Current block and posting being processed
    private BlockDescriptor currentBlock = null;
    private Posting currentPosting = null;

    // Variables used for computing BM25 parameters
    private int BM25Dl = 1;
    private int BM25Tf = 0;

    /**
     * Constructor that creates a posting list from a string
     *
     * @param toParse the string from which we can parse the posting list, with 2 formats:
     *                - [term] -> only the posting term
     *                - [term] \t [docid]:[frequency] [docid]:{frequency] ... -> the term and the posting list
     */
    public PostingList(Config configuration, String toParse) {
        config = configuration;
        // Split the input string to get the term and postings
        String[] termRow = toParse.split("\t");
        this.term = termRow[0];

        // Check if there are postings in the input string
        if (termRow.length > 1) {
            // If yes, parse the postings
            parsePostings(termRow[1]);
        }
    }

    public PostingList(Config configuration) {
        config = configuration;
    }

    /**
     * Parses the postings from a string
     *
     * @param rawPostings string with the real postings
     */
    private void parsePostings(String rawPostings) {
        // Split the string to get individual posting information
        String[] documents = rawPostings.split(" ");
        for (String elem : documents) {
            // Split each posting to get docid and frequency
            String[] posting = elem.split(":");
            postings.add(new Posting(Integer.parseInt(posting[0]), Integer.parseInt(posting[1])));
        }
    }

    // Getters and setters
    public String getTerm() {
        return term;
    }

    public ArrayList<Posting> getPostings() {
        return postings;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void appendPostings(ArrayList<Posting> newPostings){
        postings.addAll(newPostings);
    }

    // BM25 Methods

    // Methods for posting list access, debugging, and conversion are not repeated here for brevity.
    public void updateBM25Parameters(int length, int tf) {
        double currentRatio = (double) this.BM25Tf / (double) (this.BM25Dl + this.BM25Tf);
        double newRatio = (double) tf / (double) (length + tf);
        if (newRatio > currentRatio) {
            this.BM25Tf = tf;
            this.BM25Dl = length;
        }
    }

    // Getters and setters for BM25 parameters
    public int getBM25Dl() {
        return BM25Dl;
    }

    public int getBM25Tf() {
        return BM25Tf;
    }

    public void setBM25Dl(int BM25Dl) {
        this.BM25Dl = BM25Dl;
    }

    public void setBM25Tf(int BM25Tf) {
        this.BM25Tf = BM25Tf;
    }


    // Method to open the posting list
    public void openList() {
        Vocabulary v = Vocabulary.with(config);
        v.readFromDisk();
        blocks = v.get(term).readBlocks();

        if (blocks == null) {
            return;
        }
        blocksIterator = blocks.iterator();
        postingIterator = postings.iterator();
    }

    // Method to retrieve the next posting
    public Posting next(Config config) {
        if (!postingIterator.hasNext()) {
            if (!blocksIterator.hasNext()) {
                currentPosting = null;
                return null;
            }
            currentBlock = blocksIterator.next();
            postings.clear();
            postings.addAll(currentBlock.getBlockPostings(config));
            postingIterator = postings.iterator();
        }
        currentPosting = postingIterator.next();
        return currentPosting;
    }

    // Method to retrieve the last accessed posting
    public Posting getCurrentPosting() {
        return currentPosting;
    }

    // Method to retrieve the first posting with docid greater or equal than the specified docid
    public Posting selectPostingScoreIterator(int docId, Config config) {
        boolean blockChanged = false;
        while (currentBlock == null || currentBlock.getMaxDocid() < docId) {
            if (!blocksIterator.hasNext()) {
                currentPosting = null;
                return null;
            }
            currentBlock = blocksIterator.next();
            blockChanged = true;
        }
        if (blockChanged) {
            postings.clear();
            postings.addAll(currentBlock.getBlockPostings(config));
            postingIterator = postings.iterator();
        }
        while (postingIterator.hasNext()) {
            currentPosting = postingIterator.next();
            if (currentPosting.getDocId() >= docId)
                return currentPosting;
        }
        currentPosting = null;
        return null;
    }

    // Method to close the posting list
    public void closeList() {
        postings.clear();
        blocks.clear();
        Vocabulary.with(config).remove(term);
    }

    // Method to write the posting list as plain text in debug files
    public void debugSaveToDisk(String docidsPath, String freqsPath, int maxPostingsPerBlock) {
        //FileUtils.createFolder("../test/data/debug");
        //FileUtils.createFolder("../test/data/debug/" + docidsPath);
        //FileUtils.createFolder("../test/data/debug/" + freqsPath);
        //FileUtils.createFolder("../test/data/debug/completeList.txt");

        try {
            BufferedWriter writerDocids = new BufferedWriter(new FileWriter("../test/data/debug/" + docidsPath, true));
            BufferedWriter writerFreqs = new BufferedWriter(new FileWriter("../test/data/debug/" + freqsPath, true));
            BufferedWriter all = new BufferedWriter(new FileWriter("../test/data/debug/completeList.txt", true));
            String[] postingInfo = toStringPosting();
            int postingsPerBlock = 0;
            for (Posting p : postings) {
                writerDocids.write(p.getDocId() + " ");
                writerFreqs.write(p.getFrequency() + " ");
                postingsPerBlock++;
                if (postingsPerBlock == maxPostingsPerBlock) {
                    writerDocids.write("| ");
                    writerFreqs.write("| ");
                    postingsPerBlock = 0;
                }
            }
            writerDocids.write("\n");
            writerFreqs.write("\n");
            writerDocids.write(postingInfo[0] + "\n");
            writerFreqs.write(postingInfo[1] + "\n");
            all.write(this.toString());
            writerDocids.close();
            writerFreqs.close();
            all.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to convert the posting list to string format
    public String[] toStringPosting() {
        StringBuilder resultDocids = new StringBuilder();
        StringBuilder resultFreqs = new StringBuilder();
        resultDocids.append(term).append(" -> ");
        resultFreqs.append(term).append(" -> ");
        int curBlock = 0;
        int curPosting = 0;
        int numPostings = postings.size();
        int numBlocks = 1;

        if (postings.size() > 1024) {
            numBlocks = (int) Math.ceil(Math.sqrt(postings.size()));
            numPostings = (int) Math.ceil(postings.size() / (double) numBlocks);
        }

        while (curBlock < numBlocks) {
            int n = Math.min(numPostings, postings.size() - curPosting);
            for (int i = 0; i < n; i++) {
                resultDocids.append(postings.get(curPosting).getDocId());
                resultFreqs.append(postings.get(curPosting).getFrequency());
                if (i != n - 1) {
                    resultDocids.append(", ");
                    resultFreqs.append(", ");
                }
                curPosting++;
            }
            curBlock++;
            if (curBlock != numBlocks) {
                resultDocids.append(" | ");
                resultFreqs.append(" | ");
            }
        }
        return new String[]{resultDocids.toString(), resultFreqs.toString()};
    }

    // Method to convert the posting list to string format
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("\"");
        result.append(term);
        result.append('\t');
        for (Posting p : postings) {
            result.append(p.getDocId()).append(":").append(p.getFrequency()).append(" ");
        }
        result.append("\"");
        result.append('\n');
        return result.toString();
    }

}

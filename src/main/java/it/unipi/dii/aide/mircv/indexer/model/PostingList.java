package it.unipi.dii.aide.mircv.indexer.model;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import it.unipi.dii.aide.mircv.utils.FileHandler;
import java.io.*;
import java.util.*;

public class PostingList {

    private static String DEBUG_PATH = "data/debug";

    private final Config config;
    private String term;

    private final PostingStats stats = new PostingStats();

    private final ArrayList<Posting> postings = new ArrayList<>();

    private BlockDescriptor currentBlock = null;
    private Posting currentPosting = null;
    private ArrayList<BlockDescriptor> blocks = null;
    private Iterator<Posting> postingIterator = null;
    private Iterator<BlockDescriptor> blocksIterator = null;

    public static void setupPath(Config config) {
        DEBUG_PATH = config.getDebugPath();
    }

    public PostingList(Config config, String term) {
        this(config);
        this.term = term.split("\t")[0];
        String[] termRow = term.split("\t");
        if (termRow.length > 1) {
            parsePostings(termRow[1]);
        }
    }

    public void parsePostings(String rawPosting) {
        String[] documents = rawPosting.split(" ");
        for (String document : documents) {
            String[] docFreq = document.split(":");

            int docID = Integer.parseInt(docFreq[0]);
            int frequency = Integer.parseInt(docFreq[1]);

            postings.add(new Posting(docID, frequency));
        }
    }

    public PostingList(Config config) {
        this.config = config;
    }

    public void add(int docID) {
        postings.add(new Posting(docID, 1));
    }

    public ArrayList<Posting> getPostings() {
        return postings;
    }

    public boolean isEmpty() {
        return postings.isEmpty();
    }

    public boolean updateOrAddPosting(int documentId) {
        if (!postings.isEmpty()) {
            Posting posting = postings.get(postings.size() - 1);
            // If the document ID is already in the list, increment the frequency
            if (posting.getDocumentId() == documentId) {
                posting.setFrequency(posting.getFrequency() + 1);
                return true;
            }
        }

        postings.add(new Posting(documentId, 1));

        return true;
    }

    public void updateParameters(int documentsLength) {
        int tf = postings.size();

        double currentRatio = (double) stats.BM25Tf / (double) (stats.BM25Dl + stats.BM25Tf);
        double newRatio = (double) tf / (double) (documentsLength + tf);
        if (newRatio > currentRatio) {
            stats.BM25Tf = tf;
            stats.BM25Dl = documentsLength;
        }
    }

    public String getTerm() {
        return term;
    }

    public PostingStats getStats() {
        return stats;
    }

    public void debugSaveToDisk(String docidsPath, String freqsPath, int maxPostingsPerBlock){
        FileHandler.createFolderIfNotExists(DEBUG_PATH);
        FileHandler.createFileIfNotExists(DEBUG_PATH + "/" + docidsPath);
        FileHandler.createFileIfNotExists(DEBUG_PATH + "/" + freqsPath);
        FileHandler.createFileIfNotExists(DEBUG_PATH + "/" + "completeList.txt");

        try {
            BufferedWriter writerDocids = new BufferedWriter(new FileWriter(DEBUG_PATH + "/" + docidsPath, true));
            BufferedWriter writerFreqs = new BufferedWriter(new FileWriter(DEBUG_PATH + "/" + freqsPath, true));
            BufferedWriter all = new BufferedWriter(new FileWriter(DEBUG_PATH + "/" + "completeList.txt", true));
            String[] postingInfo = toStringPosting();
            int postingsPerBlock = 0;
            for(Posting p: postings){
                writerDocids.write(p.getDocumentId()+" ");
                writerFreqs.write(p.getFrequency()+" ");
                postingsPerBlock ++;
                // check if I reach the maximum number of terms per block
                if(postingsPerBlock == maxPostingsPerBlock){
                    // write the block separator on file
                    writerDocids.write("| ");
                    writerFreqs.write("| ");

                    // reset tne number of postings to zero
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

    public String[] toStringPosting() {

        StringBuilder resultDocids = new StringBuilder();
        StringBuilder resultFreqs = new StringBuilder();

        resultDocids.append(term).append(" -> ");
        resultFreqs.append(term).append(" -> ");

        int curBlock = 0;
        int curPosting = 0;
        int numPostings = postings.size();
        int numBlocks = 1;

        if(postings.size() > 1024) {
            numBlocks = (int) Math.ceil(Math.sqrt(postings.size()));
            numPostings = (int) Math.ceil( postings.size() / (double) numBlocks);
        }

        while(curBlock < numBlocks){

            //The number of postings in the last block may be greater from the actual number of postings it contains
            int n = Math.min(numPostings,postings.size() - curPosting);

            for(int i = 0; i < n; i++){
                resultDocids.append(postings.get(curPosting).getDocumentId());
                resultFreqs.append(postings.get(curPosting).getFrequency());

                if(i != n - 1) {
                    resultDocids.append(", ");
                    resultFreqs.append(", ");
                }
                curPosting++;
            }

            curBlock++;

            //there are iterations left
            if(curBlock != numBlocks ) {
                resultDocids.append(" | ");
                resultFreqs.append(" | ");
            }
        }
        return new String[]{resultDocids.toString(),resultFreqs.toString()};
    }

    public int getBM25Dl() {
        return stats.BM25Dl;
    }

    public int getBM25Tf() {
        return stats.BM25Tf;
    }

    public void append(ArrayList<Posting> postings) {
        this.postings.addAll(postings);
    }

    public int getPostingsToBeWritten(int i, int maxNumPostings) {
        int alreadyWrittenPostings = i * maxNumPostings;
        return (Math.min((this.getPostings().size() - alreadyWrittenPostings), maxNumPostings));
    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();
        result.append("\"");
        result.append(term);
        result.append('\t');
        for(Posting p: postings){
            result.append(p.getDocumentId()).append(":").append(p.getFrequency()).append(" ");
        }
        result.append("\"");
        result.append('\n');

        return result.toString();
    }

    public void closeList() {
        postings.clear();
        blocks.clear();

        Vocabulary.with(config).remove(term);
    }

    public void openList() {
        //VocabularyEntry entry = Vocabulary.with(config).getEntry(term);
        //blocks = entry.readBlocks();
        blocks = Vocabulary.getInstance().get(term).readBlocks();

        if (blocks == null) {
            return;
        }

        blocksIterator = blocks.iterator();
        postingIterator = postings.iterator();
    }

    public Posting next() {
        if(!postingIterator.hasNext()) {

            // no new blocks: end of list
            if (!blocksIterator.hasNext()) {
                currentPosting = null;
                return null;
            }

            // load the new block and update the postings iterator
            currentBlock = blocksIterator.next();
            //remove previous postings
            postings.clear();
            postings.addAll(currentBlock.getBlockPostings());
            postingIterator = postings.iterator();
        }
        // return the next posting to process
        currentPosting = postingIterator.next();
        return currentPosting;
    }

    public Posting getCurrentPosting() {
        return currentPosting;
    }

    public Posting selectPostingScoreIterator(int docId, Config config) {
        // flag to check if the block has changed
        boolean blockChanged = false;
        // move to the block with max docid >= docid
        // current block is null only if it's the first read
        while(currentBlock == null || currentBlock.getMaxDocumentsId() < docId){
            // end of list, return null
            if(!blocksIterator.hasNext()){
                currentPosting = null;
                return null;
            }

            currentBlock = blocksIterator.next();
            blockChanged = true;
        }
        // block changed, load postings and update iterator
        if(blockChanged){
            //remove previous postings
            postings.clear();
            postings.addAll(currentBlock.getBlockPostings());
            postingIterator = postings.iterator();
        }
        // move to the first GE posting and return it
        while (postingIterator.hasNext()) {
            currentPosting = postingIterator.next();
            if (currentPosting.getDocumentId() >= docId)
                return currentPosting;
        }
        currentPosting = null;
        return null;
    }

    public void updateBM25Parameters(int documentLength, int termFrequency) {
        double currentRatio = (double) this.stats.BM25Tf / (double) (this.stats.BM25Dl + this.stats.BM25Tf);
        double newRatio = (double) termFrequency / (double) (documentLength + termFrequency);
        if(newRatio > currentRatio){
            this.stats.BM25Tf = termFrequency;
            this.stats.BM25Dl = documentLength;
        }
    }

    public void setStats(int BM25Dl, int BM25Tf) {
        this.stats.BM25Dl = BM25Dl;
        this.stats.BM25Tf = BM25Tf;
    }

  public void setTerm(String term) {
        this.term = term;
    }


    public static class PostingStats {
        private int BM25Tf;
        private int BM25Dl;
    }
}

package it.unipi.dii.aide.mircv.indexer.vocabulary.entry;

import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.indexer.model.BlockDescriptor;
import it.unipi.dii.aide.mircv.indexer.vocabulary.Vocabulary;
import it.unipi.dii.aide.mircv.utils.FileHandler;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.nio.channels.FileChannel;

public class BaseVocabularyEntry {

    protected String term;
    protected VocabularyEntryUpperBoundInfo upperBoundInfo;
    protected VocabularyMemoryInfo memoryInfo;

    protected int documentFrequency;
    protected double inverseDocumentFrequency;
    protected int maxTermFrequency;

    public BaseVocabularyEntry() {
        this.memoryInfo = new VocabularyMemoryInfo();
        this.upperBoundInfo = new VocabularyEntryUpperBoundInfo();
    }

    public String getTerm() {
        return term;
    }

    public BaseVocabularyEntry(String term,
                               VocabularyEntryUpperBoundInfo upperBoundInfo,
                               VocabularyMemoryInfo memoryInfo) {
        this.term = term;
        this.upperBoundInfo = upperBoundInfo;
        this.memoryInfo = memoryInfo;
    }

    public void debugSaveToDisk(String path) {
        FileHandler.createFolderIfNotExists("data/debug/");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("data/debug/" + path, true));
            writer.write(this+"\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getMaxNumberOfPostingInBlock() {
        return memoryInfo.getMaxNumberOfPostingInBlock(documentFrequency);
    }

    public int getBM25Tf() {
        return upperBoundInfo.BM25Tf;
    }

    public int getBM25Dl() {
        return upperBoundInfo.BM25Dl;
    }

    public void updateBM25Statistics(double bm25Tf, double bm25Dl) {
        upperBoundInfo.updateBM25Parameters((int) bm25Dl, (int) bm25Tf);
    }

    public void updateMemoryIdSize(int numPostings) {
        this.memoryInfo.setDocIdSize(numPostings * 4);
        this.memoryInfo.setFrequencySize(numPostings * 4);
    }

    public void setDocumentIdOffset(int position) {
        this.memoryInfo.setDocumentIdOffset(position);
    }

    public void setFrequencyOffset(int position) {
        this.memoryInfo.setFrequencyOffset(position);
    }

    public void setBM25Tf(int bm25Tf) {
        this.upperBoundInfo.setBM25Tf(bm25Tf);
    }

    public void setDocumentFrequency(int documentFrequency) {
        this.documentFrequency = documentFrequency;
    }

    public void setMaxTermFrequency(int maxTermFrequency) {
        this.maxTermFrequency = maxTermFrequency;
    }

    public void setBM25Dl(int BM25Dl) {
        this.upperBoundInfo.setBM25Dl(BM25Dl);
    }

    //
    public static class VocabularyEntryUpperBoundInfo {
        private int BM25Dl = 1;
        private int BM25Tf = 0;

        private double maxTfIdf = 0;

        private double maxBM25 = 0;

        public VocabularyEntryUpperBoundInfo() {}

        public VocabularyEntryUpperBoundInfo(int BM25Dl, int BM25Tf, int maxTfIdf, int maxBM25) {
            this.BM25Dl = BM25Dl;
            this.BM25Tf = BM25Tf;
            this.maxTfIdf = maxTfIdf;
            this.maxBM25 = maxBM25;
        }


        public void mapVocabularyEntryStats(VocabularyEntryUpperBoundInfo stats, MappedByteBuffer buffer) {
            stats.BM25Dl = buffer.getInt();
            stats.BM25Tf = buffer.getInt();
            stats.maxTfIdf = buffer.getDouble();
            stats.maxBM25 = buffer.getDouble();
        }

        public void writeBufferWithEntryStats(VocabularyEntryUpperBoundInfo stats, MappedByteBuffer buffer) {
            buffer.putInt(stats.BM25Dl);
            buffer.putInt(stats.BM25Tf);
            buffer.putDouble(stats.maxTfIdf);
            buffer.putDouble(stats.maxBM25);
        }

        public void updateBM25Parameters(int length, int tf) {
            double currentRatio = (double) this.BM25Tf / (double) (this.BM25Dl + this.BM25Tf);
            double newRatio = (double) tf / (double) (length + tf);
            if (newRatio > currentRatio) {
                this.BM25Tf = tf;
                this.BM25Dl = length;
            }
        }

        public double getIdf() {
            return Math.log((double) BM25Dl / (double) BM25Tf);
        }

        public void setBM25Tf(int bm25Tf) {
            BM25Tf = bm25Tf;
        }

        public void setBM25Dl(int bm25Dl) {
            BM25Dl = bm25Dl;
        }

        public void computeUpperBounds(double inverseDocumentFrequency) {
            this.maxTfIdf = (1 + Math.log10(this.maxTfIdf)) * inverseDocumentFrequency;

            double k1 = 1.5;
            double b = 0.75;
            double avgDocLen = (double) DocumentIndexState.getTotalDocumentLen() / DocumentIndexState.getCollectionSize();

            this.maxBM25 = (inverseDocumentFrequency * BM25Tf)  / ( BM25Tf + k1 * (1 - b + b * (double)BM25Dl/avgDocLen));
        }

        public double getMaxBM25Tf() {
            return maxBM25;
        }

        public double getMaxTfIdf() {
            return maxTfIdf;
        }

        public int getBM25Tf() {
            return BM25Tf;
        }
    }

    // 36 bytes
    public static class VocabularyMemoryInfo {
        public static final int BLOCK_DESCRIPTOR_ENTRY_BYTES = 4 * 4 + 2 * 8;

        public static long memoryOffset = 0;

        private long docIdOffset=0;
        private int docIdSize=0;
        private long frequencyOffset=0;
        private int frequencySize=0;
        private int numBlocks=1;
        private long blockOffset=0;

        public VocabularyMemoryInfo(long docIdOffset, int docIdSize, long frequencyOffset, int frequencySize, int numBlocks, long blockOffset) {
            this.docIdOffset = docIdOffset;
            this.docIdSize = docIdSize;
            this.frequencyOffset = frequencyOffset;
            this.frequencySize = frequencySize;
            this.numBlocks = numBlocks;
            this.blockOffset = blockOffset;
        }

        public VocabularyMemoryInfo(int documentIdPosition, int freqsPosition, int documentIdSize, int frequencyOffset) {
            this.docIdOffset = documentIdPosition;
            this.docIdSize = documentIdSize;
            this.frequencyOffset = freqsPosition;
            this.frequencySize = frequencyOffset;
        }

        public VocabularyMemoryInfo() {
            this.docIdOffset = 0;
            this.frequencyOffset = 0;
            this.docIdSize = 0;
            this.frequencySize = 0;
        }

        public void mapVocabularyMemoryInfo(VocabularyMemoryInfo memoryInfo, MappedByteBuffer buffer) {
            memoryInfo.docIdOffset = buffer.getLong();
            memoryInfo.frequencyOffset = buffer.getLong();
            memoryInfo.docIdSize = buffer.getInt();
            memoryInfo.frequencySize = buffer.getInt();
            memoryInfo.numBlocks = buffer.getInt();
            memoryInfo.blockOffset = buffer.getLong();
        }

        public void writeBufferWithMemoryInfo(VocabularyMemoryInfo memoryInfo, MappedByteBuffer buffer) {
            buffer.putLong(memoryInfo.docIdOffset);
            buffer.putLong(memoryInfo.frequencyOffset);
            buffer.putInt(memoryInfo.docIdSize);
            buffer.putInt(memoryInfo.frequencySize);
            buffer.putInt(memoryInfo.numBlocks);
            buffer.putLong(memoryInfo.blockOffset);

            memoryOffset += BLOCK_DESCRIPTOR_ENTRY_BYTES;
        }

        public long getDocumentIdOffset() {
            return docIdOffset;
        }


        public int getDocumentIdSize() {
            return docIdSize;
        }

        public long getFrequencyOffset() {
            return frequencyOffset;
        }

        public long getFrequencySize() {
            return frequencySize;
        }

        public void computeBlockInformation(int documentFrequency) {
            this.blockOffset = memoryOffset;
            if (documentFrequency >= 1024)
                this.numBlocks = (int) Math.ceil(Math.sqrt(documentFrequency));
        }

        public int getMaxNumberOfPostingInBlock(int documentFrequency) {
            return (int) Math.ceil(documentFrequency / (double) this.numBlocks);
        }

        public int getHowManyBlockToWrite() {
            return numBlocks;
        }

        public ArrayList<BlockDescriptor> readBlocks() {
            ArrayList<BlockDescriptor> blocks = new ArrayList<>();

            try (FileChannel channel = FileChannelHandler.open(Vocabulary.PATH,
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE
            )) {
                for (int i = 0; i < numBlocks; i++) {
                    MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, blockOffset, BLOCK_DESCRIPTOR_ENTRY_BYTES);
                    BlockDescriptor block = new BlockDescriptor();

                    block.mapBlockDescriptor(buffer);

                    blocks.add(block);
                    blockOffset += 16;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return blocks;
        }

        public void setDocumentIdOffset(long docummentsMemOffset) {
            this.docIdOffset = docummentsMemOffset;
        }

        public void setFrequencyOffset(long frequencyMemOffset) {
            this.frequencyOffset = frequencyMemOffset;
        }

        public void setDocIdSize(int docIdSize) {
            this.docIdSize = docIdSize;
        }

        public void setFrequencySize(int frequencySize) {
            this.frequencySize = frequencySize;
        }
    }

    public String toString() {
        return ", term='" + term + '\'' +
                ", df=" + documentFrequency +
                ", idf=" + inverseDocumentFrequency +
                ", maxTf=" + maxTermFrequency +
                ", BM25Dl=" + upperBoundInfo.BM25Dl +
                ", BM25Tf=" + upperBoundInfo.BM25Tf +
                ", maxTFIDF=" + upperBoundInfo.maxTfIdf +
                ", maxBM25=" + upperBoundInfo.maxBM25 +
                ", docidOffset=" + memoryInfo.docIdOffset +
                ", frequencyOffset=" + memoryInfo.frequencyOffset +
                ", docidSize=" + memoryInfo.docIdSize +
                ", frequencySize=" + memoryInfo.frequencySize +
                ", numBlocks=" + memoryInfo.numBlocks +
                ", blockOffset=" + memoryInfo.blockOffset;
    }
}

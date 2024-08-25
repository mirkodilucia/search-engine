package it.unipi.dii.aide.mircv.indexer.vocabulary.entry;

import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.indexer.model.BlockDescriptor;
import it.unipi.dii.aide.mircv.indexer.model.Posting;
import it.unipi.dii.aide.mircv.indexer.model.PostingList;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class VocabularyEntry extends BaseVocabularyEntry {

    private static final int TERM_SIZE = 64;
    public static final long ENTRY_SIZE = TERM_SIZE + 76;

    /** Default constructor
     * use it to read from disk
     * */
    public VocabularyEntry() {
        super();

    }

    /** Constructor with the term
     * use it to search for an entry
     * */
    public VocabularyEntry(String term) {
        super();
        this.term = term;
        this.upperBoundInfo = new VocabularyEntryUpperBoundInfo();
        this.memoryInfo = new VocabularyMemoryInfo();
    }

    /** Constructor with all the fields
     * use it to create a new entry and write to disk
     * */
    public VocabularyEntry(String term,
                           VocabularyEntryUpperBoundInfo stats,
                           VocabularyMemoryInfo memoryInfo
    ) {
        super(term, stats, memoryInfo);
    }

    public VocabularyEntry(String term,
                           int documentFrequency,
                           double inverseDocumentFrequency,
                           VocabularyEntryUpperBoundInfo stats,
                           VocabularyMemoryInfo memoryInfo
    ) {
        super(term, documentFrequency, inverseDocumentFrequency, stats, memoryInfo);
    }

    public long readVocabularyFromDisk(long memoryOffset, String vocabularyFilePath) {
        try (
                FileChannel vocabularyChannel = FileChannelHandler.open(vocabularyFilePath,
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE
                );
        ) {
            long readVocabularyResult = readVocabularyFromDisk(memoryOffset, vocabularyChannel);
            long readBlockResult = readBlockDescriptorFromDisk(memoryOffset, vocabularyChannel);

            if (readVocabularyResult == -1 || readBlockResult == -1) {
                return -1;
            }

            if (readVocabularyResult == 0 || readBlockResult == 0) {
                return 0;
            }

            return memoryOffset + ENTRY_SIZE;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public long readVocabularyFromDisk(long memoryOffset, FileChannel vocabularyFile) {
        try {
            // Open the buffer to read the term
            MappedByteBuffer buffer = vocabularyFile.map(FileChannel.MapMode.READ_ONLY, memoryOffset, ENTRY_SIZE);
            if (buffer == null)
                return -1;

            // Decode the term
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
            String[] encodedTerm = charBuffer.toString().split("\0");
            if (encodedTerm.length == 0)
                return 0;

            this.term = encodedTerm[0];

            return ENTRY_SIZE + memoryOffset;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public long readBlockDescriptorFromDisk(long memoryOffset, FileChannel blockDescriptorFile) {
        try {
            // Open the buffer to read the block and stats
            MappedByteBuffer buffer = blockDescriptorFile.map(FileChannel.MapMode.READ_ONLY, memoryOffset + TERM_SIZE, ENTRY_SIZE - TERM_SIZE);
            if (buffer == null)
                return -1;

            documentFrequency = buffer.getInt();
            inverseDocumentFrequency = buffer.getDouble();

            // Read the block and stats
            upperBoundInfo.mapVocabularyEntryStats(upperBoundInfo, buffer);
            memoryInfo.mapVocabularyMemoryInfo(memoryInfo, buffer);

            return ENTRY_SIZE + memoryOffset;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public long writeEntry(long vocOffset, FileChannel vocabularyFileChannel) {
        try {
            // Write the term
            MappedByteBuffer buffer = vocabularyFileChannel.map(FileChannel.MapMode.READ_WRITE, vocOffset, ENTRY_SIZE);
            if (buffer == null)
                return -1;

            CharBuffer charBuffer = CharBuffer.allocate(TERM_SIZE);
            for (int i = 0; i < term.length(); i++) {
                if (i >= TERM_SIZE)
                    break;
                charBuffer.put(i, term.charAt(i));
            }

            buffer.put(StandardCharsets.UTF_8.encode(charBuffer));

            buffer.putInt(documentFrequency);
            buffer.putDouble(inverseDocumentFrequency);

            upperBoundInfo.writeBufferWithEntryStats(upperBoundInfo, buffer);
            memoryInfo.writeBufferWithMemoryInfo(memoryInfo, buffer);

            return vocOffset + ENTRY_SIZE;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public double getIdf() {
        return inverseDocumentFrequency;// upperBoundInfo.getIdf();
    }

    public void updateStatistics(PostingList entry) {
        for (Posting posting: entry.getPostings()) {
            if (this.upperBoundInfo.maxTermFrequency < posting.getFrequency()) {
                this.upperBoundInfo.maxTermFrequency = posting.getFrequency();
            }

            this.documentFrequency++;
        }
    }

    public int getDocumentFrequency() {
        return documentFrequency;
    }

    public void update(long documentMemoryOffset, long frequencyMemOffset) {
        this.memoryInfo.setDocumentIdOffset(documentMemoryOffset);
        this.memoryInfo.setFrequencyOffset(frequencyMemOffset);

        //this.inverseDocumentFrequency = (int) Math.log10(DocumentIndexState.getCollectionSize() / (double) this.documentFrequency);
        this.upperBoundInfo.computeUpperBounds(inverseDocumentFrequency);
    }

    public VocabularyMemoryInfo getMemoryInfo() {
        return memoryInfo;
    }

    public void computeBlockInformation() {
        this.memoryInfo.computeBlockInformation(documentFrequency);
    }

    public int getHowManyBlockToWrite() {
        return memoryInfo.getHowManyBlockToWrite();
    }

    public double getInverseDocumentFrequency() {
        return inverseDocumentFrequency;
    }

    public double getMaxBM25Tf() {
        return upperBoundInfo.getMaxBM25Tf();
    }


    public double getMaxTfIdf() {
        return upperBoundInfo.getMaxTfIdf();
    }

    public ArrayList<BlockDescriptor> readBlocks() {
        return memoryInfo.readBlocks();
    }
}

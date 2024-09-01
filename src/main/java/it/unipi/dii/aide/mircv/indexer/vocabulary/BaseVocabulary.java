package it.unipi.dii.aide.mircv.indexer.vocabulary;

import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;
import org.junit.platform.commons.util.LruCache;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Objects;

public class BaseVocabulary extends LinkedHashMap<String, VocabularyEntry> {

    public static String VOCABULARY_PATH;
    public static String BLOCK_DESCRIPTOR_PATH;

    protected FileChannel vocabularyFileChannel;

    private final static LruCache<String, VocabularyEntry> entries = new LruCache<>(1000);

    public BaseVocabulary(String vocabularyPath, String blockDescriptorsPath) {
        VOCABULARY_PATH = Objects.requireNonNullElse(vocabularyPath, "data/vocabulary/vocabulary_0.dat");
        BLOCK_DESCRIPTOR_PATH = Objects.requireNonNullElse(blockDescriptorsPath, "data/vocabulary/block_descriptor.dat");

        try {
            vocabularyFileChannel = FileChannelHandler.open(VOCABULARY_PATH,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.READ,
                    StandardOpenOption.CREATE
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BaseVocabulary() {

    }

    /**
     * Find the VocabularyEntry associated with the term using a binary search
     * @param term the term to search
     * @return the VocabularyEntry associated with the term
     */
    VocabularyEntry findEntry(String term) {
        VocabularyEntry entry = new VocabularyEntry();

        long start = 0;
        long end = DocumentIndexState.getVocabularySize() - 1;
        long mid = 0;

        String key;

        while (start <= end) {
            mid = start + (end - start) / 2;

            entry.readVocabularyFromDisk(VocabularyEntry.ENTRY_SIZE * mid, VOCABULARY_PATH);
            key = entry.getTerm();

            if (key.equals(term)) {
                return entry;
            }

            if (key.compareTo(term) < 0) {
                start = mid + 1;
                continue;
            }

            end = mid - 1;
        }

        return null;
    }

    /**
     * Read the vocabulary from disk
     * @return true if the operation was successful, false otherwise
     */
    public boolean readFromDisk(){

        long position = 0;

        //read whole vocabulary from
        while(position >= 0){
            VocabularyEntry entry = new VocabularyEntry();
            //read entry and update position
            position = entry.readVocabularyFromDisk(position, VOCABULARY_PATH);

            if(position == 0)
                return  true;

            if(entry.getTerm()==null){
                return true;
            }

            if (entry.getTerm().isEmpty()) {
                return true;
            }

            //populate vocabulary
            this.put(entry.getTerm(),entry);
            entries.put(entry.getTerm(),entry);
        }

        //if position == -1 an error occurred during reading
        return position != -1;
    }

    /**
     * Write the vocabulary to disk
     * @return true if the operation was successful, false otherwise
     */
    public static void clearCache() {
        entries.clear();
    }

    /**
     * Write the vocabulary to disk
     * @return true if the operation was successful, false otherwise
     */
    public void reset() {
        clear();
        clearCache();

        try {
            vocabularyFileChannel.close();

            vocabularyFileChannel = FileChannelHandler.open(VOCABULARY_PATH,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.READ,
                    StandardOpenOption.CREATE
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

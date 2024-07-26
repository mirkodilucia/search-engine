package it.unipi.dii.aide.mircv.indexer.vocabulary;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.indexer.model.BlockDescriptor;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import org.junit.platform.commons.util.LruCache;

import java.io.IOException;
import java.nio.channels.FileChannel;

public class Vocabulary extends BaseVocabulary {

    private static Vocabulary instance = null;
    private final static LruCache<String, VocabularyEntry> entries = new LruCache<>(1000);

    private Vocabulary(String vocabularyPath, String blockDescriptorsPath) {
        super(vocabularyPath, blockDescriptorsPath);
    }

    public static Vocabulary with(Config config) {
        BlockDescriptor.init(config);
        DocumentIndexState.with(config);
        if(instance == null) {
            instance = new Vocabulary(
                    config.getVocabularyPath(),
                    config.getBlockDescriptorsPath()
            );
        }
        return instance;
    }

    public void unset() {
        instance = null;
        this.clear();

        try {
            vocabularyFileChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double getIdf(String term){
        return get(term).getIdf();
    }

    public VocabularyEntry getEntry(String term){
        if(entries.containsKey(term))
            return entries.get(term);

        VocabularyEntry entry = findEntry(term);
        entries.put(term, entry);
        return entry;
    }

    public void reset() {
        super.reset();
    }

    public FileChannel getVocabularyChannel() {
        return vocabularyFileChannel;
    }

    public VocabularyEntry[] getVocabularyEntries() {
        return this.values().toArray(new VocabularyEntry[0]);
    }
}

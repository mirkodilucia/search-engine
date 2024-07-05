package it.unipi.dii.aide.mircv.indexer.vocabulary;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import org.junit.platform.commons.util.LruCache;

import java.nio.channels.FileChannel;

public class Vocabulary extends BaseVocabulary {

    private static Vocabulary instance = null;
    private final static LruCache<String, VocabularyEntry> entries= new LruCache<>(1000);

    private Vocabulary(String vocabularyPath, String blockDescriptorsPath) {
        super(vocabularyPath, blockDescriptorsPath);
    }

    public Vocabulary() {
        super();
    }

    /**
     * singleton pattern
     */
    public static Vocabulary getInstance(){
        if(instance == null){
            instance = new Vocabulary();
        }
        return instance;
    }

    public static Vocabulary with(Config config){
        DocumentIndexState.with(config);
        if(instance == null) {
            instance = new Vocabulary(
                    config.getVocabularyPath(),
                    config.getBlockDescriptorsPath()
            );
        }
        return instance;
    }

    public double getIdf(String term){
        return getEntry(term).getIdf();
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
}

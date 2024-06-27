package it.unipi.dii.aide.mircv.indexer.vocabulary;


import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;
import org.junit.platform.commons.util.LruCache;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class Vocabulary extends BaseVocabulary {

    private static Vocabulary instance = null;
    private final static LruCache<String, VocabularyEntry> entries= new LruCache<>(1000);

    private Vocabulary(String path){
        super(path);
    }

    public static Vocabulary with(Config config){
        if(instance == null){
            instance = new Vocabulary("data/vocabulary/vocabulary_0.dat");
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


}

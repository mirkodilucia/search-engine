package it.unipi.dii.aide.mircv.indexer.model;

import it.unipi.dii.aide.mircv.config.Config;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Vocabulary {

    private final Config config;
    private final Map<String, PostingList> index;

    public Vocabulary(Config config) {
        this.config = config;
        index = new HashMap<>();
    }

    public void addTerm(String term, int docID) {
        PostingList postingList = index.getOrDefault(term, new PostingList(config));
        postingList.add(docID);
        index.put(term, postingList);
    }

    public Map<String, PostingList> getIndex() {
        return index;
    }
}

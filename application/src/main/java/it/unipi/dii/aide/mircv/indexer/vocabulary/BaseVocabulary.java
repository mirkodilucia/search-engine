package it.unipi.dii.aide.mircv.indexer.vocabulary;

import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;

public class BaseVocabulary extends LinkedHashMap<String, VocabularyEntry> {

    private final FileChannel channel;

    public BaseVocabulary(String path) {
        try {
            channel = FileChannelHandler.open(path,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.READ,
                    StandardOpenOption.CREATE
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    VocabularyEntry findEntry(String term) {
        VocabularyEntry entry = new VocabularyEntry();

        long start = 0;
        long end = DocumentIndexState.getVocabularySize() - 1;
        long mid = 0;

        String key;

        while (start <= end) {
            mid = (start + end) / 2;

            entry.readFromDisk(VocabularyEntry.ENTRY_SIZE * mid, channel);
            key = entry.getTerm();

            if (key.equals(term)) {
                break;
            }

            if (key.compareTo(term) < 0) {
                start = mid + 1;
                continue;
            }

            end = mid - 1;
        }

        return entry;
    }
}

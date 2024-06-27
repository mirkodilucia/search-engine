package indexer.spimi.model;

import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.BaseVocabularyEntry;
import it.unipi.dii.aide.mircv.indexer.vocabulary.entry.VocabularyEntry;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class VocabularyEntryTest {

    @Test
    public void testVocabularyEntry() {
        VocabularyEntry vocabularyEntry = new VocabularyEntry("test",
                    new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                    1, 1, 1, 1),
                    new BaseVocabularyEntry.VocabularyMemoryInfo(
                        0, 0, 0, 0, 0, 0
                    )
                );

        assert vocabularyEntry.getTerm().equals("test");
        assert vocabularyEntry.getMaxNumberOfPostingInBlock() == 0;
        assert vocabularyEntry.getDocumentFrequency() == 0;
    }

    @Test
    public void testVocabularyEntryWrite() {
        try {
            FileChannel channel = FileChannelHandler.open("data_test/vocabularyEntry/vocabulary_test.dat",
                    StandardOpenOption.WRITE,
                    StandardOpenOption.READ,
                    StandardOpenOption.CREATE
            );

            VocabularyEntry vocabularyEntry = new VocabularyEntry("test",
                    new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                            1, 1, 1, 1),
                    new BaseVocabularyEntry.VocabularyMemoryInfo(
                            23, 12, 1224, 232, 10, 0
                    )
            );

            vocabularyEntry.writeEntry(0, channel);

            VocabularyEntry vocabularyEntryRead = new VocabularyEntry();
            vocabularyEntryRead.readFromDisk(0, channel);

            assert vocabularyEntryRead.getTerm().equals("test");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testVocabularyEntryMultipleWrite() {
        try {
            FileChannel channel = FileChannelHandler.open("data_test/vocabularyEntry/vocabulary_test_1.dat",
                    StandardOpenOption.WRITE,
                    StandardOpenOption.READ,
                    StandardOpenOption.CREATE
            );

            VocabularyEntry vocabularyEntry = new VocabularyEntry("test",
                    new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                            1, 1, 1, 1),
                    new BaseVocabularyEntry.VocabularyMemoryInfo(
                            0, 0, 0, 0, 0, 0
                    )
            );
            long offset = vocabularyEntry.writeEntry(0,channel);
            VocabularyEntry vocabularyEntry1 = new VocabularyEntry("test_1",
                    new BaseVocabularyEntry.VocabularyEntryUpperBoundInfo(
                            1, 1, 1, 1),
                    new BaseVocabularyEntry.VocabularyMemoryInfo(
                            0, 0, 0, 0, 0, 0
                    )
            );
            vocabularyEntry1.writeEntry(offset, channel);

            VocabularyEntry vocabularyEntryRead = new VocabularyEntry();
            long size = vocabularyEntryRead.readFromDisk(0, "data_test/vocabularyEntry/vocabulary_test_1.dat");

            assert vocabularyEntryRead.getTerm().equals("test");

            VocabularyEntry vocabularyEntryRead1 = new VocabularyEntry();
            vocabularyEntryRead1.readFromDisk(size, "data_test/vocabularyEntry/vocabulary_test_1.dat");

            assert vocabularyEntryRead1.getTerm().equals("test_1");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

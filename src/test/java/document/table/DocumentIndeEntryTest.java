package document.table;

import it.unipi.dii.aide.mircv.config.Config;
import it.unipi.dii.aide.mircv.document.table.DocumentIndexEntry;
import it.unipi.dii.aide.mircv.utils.FileChannelHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentIndeEntryTest {

    private static Config config;

    @BeforeAll
    static void init() {
        config = new Config();
    }


    @Test
    void writeReadFromZero() {
        try (
            FileChannel documentIndexFile = FileChannelHandler.open(
                    "data_test/documentIndexEntry/document_index_entry_test.dat",
                    StandardOpenOption.WRITE,
                    StandardOpenOption.READ,
                    StandardOpenOption.CREATE
            )) {

            DocumentIndexEntry entry1 = new DocumentIndexEntry(config, "test1", 0, 10);

            long offset1 = entry1.writeFile(documentIndexFile);
            assertEquals(0, offset1);

            DocumentIndexEntry readEntry1 = new DocumentIndexEntry(config, 0);
            assertTrue(readEntry1.readFile(offset1, documentIndexFile));

            assertEquals(entry1.toString(), readEntry1.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void writeReadSubsequent() {
        try (
                FileChannel documentIndexFile = FileChannelHandler.open(
                        "data_test/documentIndexEntry/document_index_entry_test_1.dat",
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE
                )) {
            DocumentIndexEntry entry1 = new DocumentIndexEntry(config, "test1", 0, 10);
            DocumentIndexEntry entry2 = new DocumentIndexEntry(config, "test2", 1, 15);

            long offset1 = entry1.writeFile(documentIndexFile);
            assertEquals(0, offset1);
            long offset2 = entry2.writeFile(documentIndexFile);
            assertEquals(72, offset2);

            DocumentIndexEntry readEntry1 = new DocumentIndexEntry(config, 0);
            assertTrue(readEntry1.readFile(offset1, documentIndexFile));
            DocumentIndexEntry readEntry2 = new DocumentIndexEntry(config, 1);
            assertTrue(readEntry2.readFile(offset2, documentIndexFile));

            assertEquals(entry1.toString(), readEntry1.toString());
            assertEquals(entry2.toString(), readEntry2.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void resetDocumentIndexEntry() {
        DocumentIndexEntry.reset();
    }
}

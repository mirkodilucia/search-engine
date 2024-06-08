package it.unipi.dii.aide.mircv.application.document.data;

import it.unipi.dii.aide.mircv.application.ConfigUtils;
import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.DocumentIndexEntry;
import it.unipi.dii.aide.mircv.application.utils.FileUtils;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class DocumentIndexEntryTest {

    private static Config config;

    @BeforeAll
    static void init() {
        config = ConfigUtils.getConfig("documentIndexTest");
    }

    @BeforeEach
    void updatePath() {
        DocumentIndexEntry.setTestPath();
    }

    @Test
    void writeReadFromZero() {
        DocumentIndexEntry entry1 = new DocumentIndexEntry(config, "test1", 0, 10);

        long offset1 = entry1.writeFile();
        assertEquals(0, offset1);

        DocumentIndexEntry readEntry1 = new DocumentIndexEntry(config);
        assertTrue(readEntry1.readFile(offset1));

        assertEquals(entry1.toString(), readEntry1.toString());
    }

    @Test
    void writeReadSubsequent() {
        DocumentIndexEntry entry1 = new DocumentIndexEntry(config, "test1", 0, 10);
        DocumentIndexEntry entry2 = new DocumentIndexEntry(config, "test2", 1, 15);

        long offset1 = entry1.writeFile();
        assertEquals(0, offset1);
        long offset2 = entry2.writeFile();
        assertEquals(72, offset2);

        DocumentIndexEntry readEntry1 = new DocumentIndexEntry(config);
        assertTrue(readEntry1.readFile(offset1));
        DocumentIndexEntry readEntry2 = new DocumentIndexEntry(config);
        assertTrue(readEntry2.readFile(offset2));

        assertEquals(entry1.toString(), readEntry1.toString());
        assertEquals(entry2.toString(), readEntry2.toString());
    }

    @AfterEach
    void deleteTestFile() {
        FileUtils.removeFile("../test/data/testDocumentIndex");
    }

}
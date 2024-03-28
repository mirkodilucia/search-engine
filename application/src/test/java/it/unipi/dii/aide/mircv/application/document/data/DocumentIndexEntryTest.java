package it.unipi.dii.aide.mircv.application.document.data;

import it.unipi.dii.aide.mircv.application.data.DocumentIndexEntry;
import it.unipi.dii.aide.mircv.application.utils.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentIndexEntryTest {

    @BeforeEach
    void updatePath() {
        DocumentIndexEntry.setTestPath();
    }

    @Test
    void writeReadFromZero() {
        DocumentIndexEntry entry1 = new DocumentIndexEntry("test1", 0, 10);

        long offset1 = entry1.writeFile();
        assertEquals(-1, offset1);

        DocumentIndexEntry readEntry1 = new DocumentIndexEntry("test1", 0, 10);
        assertTrue(readEntry1.readFile(offset1));

        assertEquals(entry1.toString(), readEntry1.toString());
    }

    @Test
    void writeReadSubsequent() {
        DocumentIndexEntry entry1 = new DocumentIndexEntry("test1", 0, 10);
        DocumentIndexEntry entry2 = new DocumentIndexEntry("test2", 1, 15);

        long offset1 = entry1.writeFile();
        assertEquals(-1, offset1);
        long offset2 = entry2.writeFile();
        assertEquals(72, offset2);

        DocumentIndexEntry readEntry1 = new DocumentIndexEntry("test0", 0, 10);
        assertTrue(readEntry1.readFile(offset1));
        DocumentIndexEntry readEntry2 = new DocumentIndexEntry("test0", 0, 10);
        assertTrue(readEntry2.readFile(offset2));

        assertEquals(entry1.toString(), readEntry1.toString());
        assertEquals(entry2.toString(), readEntry2.toString());
    }

    @AfterAll
    static void deleteTestFile() {
        FileUtils.removeFile("../test/data/testDocumentIndex");
    }

}
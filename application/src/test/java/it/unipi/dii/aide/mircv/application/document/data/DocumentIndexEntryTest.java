package document.data;


import it.unipi.dii.aide.mircv.document.data.DocumentIndexEntry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.unipi.dii.aide.mircv.utils.FileUtils;
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
        assertEquals(0, offset1);

        DocumentIndexEntry readEntry1 = new DocumentIndexEntry();
        assertTrue(readEntry1.readFile(offset1));

        assertEquals(entry1.toString(), readEntry1.toString());
    }

    @Test
    void writeReadSubsequent() {
        DocumentIndexEntry entry1 = new DocumentIndexEntry("test1", 0, 10);
        DocumentIndexEntry entry2 = new DocumentIndexEntry("test2", 1, 15);

        long offset1 = entry1.writeFile();
        assertEquals(0, offset1);
        long offset2 = entry2.writeFile();
        assertEquals(72, offset2);

        DocumentIndexEntry readEntry1 = new DocumentIndexEntry();
        assertTrue(readEntry1.readFile(offset1));
        DocumentIndexEntry readEntry2 = new DocumentIndexEntry();
        assertTrue(readEntry2.readFile(offset2));

        assertEquals(entry1.toString(), readEntry1.toString());
        assertEquals(entry2.toString(), readEntry2.toString());
    }

    @AfterAll
    static void deleteTestFile() {
        FileUtils.removeFile("src/test/testDocumentIndex");
    }

}
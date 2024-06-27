package utils;

import it.unipi.dii.aide.mircv.utils.FileHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FileHandlerTest {

    @Test
    public void testWriteReadObjectToFile() throws IOException, ClassNotFoundException {
        String fileName = "test_object.dat";
        TestObject originalObject = new TestObject("Test", 123);

        // Write the object to file
        FileHandler.writeObjectToFile(fileName, originalObject);

        // Read the object from file
        TestObject readObject = (TestObject) FileHandler.readObjectFromFile(fileName);

        // Verify that the object was correctly written and read
        assertNotNull(readObject);
        assertEquals(originalObject.getName(), readObject.getName());
        assertEquals(originalObject.getValue(), readObject.getValue());
    }

    // Helper class for testing purposes
    static class TestObject implements Serializable {
        private String name;
        private int value;

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }
}
package compression;

import it.unipi.dii.aide.mircv.compression.VariableByteCompression;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

public class VariableByteCompressionTest
{

        // Given An Array Of Integers Should Return An Array Of Bytes
        @Test
        public void encode()
        {
            int[] input = {1, 2, 3, 4, 5};
            byte[] actualResult = VariableByteCompression.encode(input);

            byte[] expectedResult = {1, 2, 3, 4, 5};
            assertArrayEquals(expectedResult, actualResult);
        }

        // Given An Array Of Bytes Should Return An Array Of Integers
        @Test
        public void decode()
        {
            byte[] input = {1, 2, 3, 4, 5};
            int[] actualResult = VariableByteCompression.decode(input);

            int[] expectedResult = {1, 2, 3, 4, 5};
            assertArrayEquals(expectedResult, actualResult);
        }












}

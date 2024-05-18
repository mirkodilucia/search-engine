package it.unipi.dii.aide.mircv.application.compression;

import org.junit.Assert;
import org.junit.Test;

public class VariableByteCompressorTest
{

        // Given An Array Of Integers Should Return An Array Of Bytes
        @Test
        public void encode()
        {
            int[] input = {1, 2, 3, 4, 5};
            byte[] actualResult = VariableByteCompressor.encode(input);

            byte[] expectedResult = {1, 2, 3, 4, 5};
            Assert.assertArrayEquals(expectedResult, actualResult);
        }

        // Given An Array Of Bytes Should Return An Array Of Integers
        @Test
        public void decode()
        {
            byte[] input = {1, 2, 3, 4, 5};
            int[] actualResult = VariableByteCompressor.decode(input, 5);

            int[] expectedResult = {1, 2, 3, 4, 5};
            Assert.assertArrayEquals(expectedResult, actualResult);
        }












}

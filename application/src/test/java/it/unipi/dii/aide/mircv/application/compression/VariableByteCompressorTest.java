package it.unipi.dii.aide.mircv.application.compression;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class VariableByteCompressorTest
{

        @Test
        public void integerArrayCompression() {
            assertArrayEquals(new byte[]{(byte) 133}, VariableByteCompressor.integerArrayCompression(new int[]{5}));
            assertArrayEquals(new byte[]{(byte) 6, (byte) 184}, VariableByteCompressor.integerArrayCompression(new int[]{824}));
            assertArrayEquals(new byte[]{(byte) 6, (byte) 184,(byte) 133}, VariableByteCompressor.integerArrayCompression(new int[]{824, 5}));
        }

        @Test
        public void integerArrayDecompression() {
            assertArrayEquals(new int[]{5}, VariableByteCompressor.decode(new byte[]{(byte) 133}, 1));
            assertArrayEquals(new int[]{824}, VariableByteCompressor.decode(new byte[]{(byte) 6, (byte) 184}, 1));
            assertArrayEquals(new int[]{824, 5}, VariableByteCompressor.decode(new byte[]{(byte) 6, (byte) 184,(byte) 133}, 2));
        }

        // Given An Array Of Bytes Should Return An Array Of Integers
        @Test
        public void decode_1()
        {
            byte[] input = { (byte) 133 };
            int[] expectedResult = { 5 };

            int[] actualResult = VariableByteCompressor.decode(input, 1);

            Assert.assertArrayEquals(expectedResult, actualResult);
        }

        @Test
        public void decode_2() {
            byte[] input = {(byte) 6, (byte) 184};
            int[] expectedResult = {824};

            int[] actualResult = VariableByteCompressor.decode(input, 1);
            Assert.assertArrayEquals(expectedResult, actualResult);
        }

        @Test
        public void decode_3() {
            byte[] input = {(byte) 6, (byte) 184,(byte) 133};
            int[] expectedResult = {824, 5};

            int[] actualResult = VariableByteCompressor.decode(input, 2);
            Assert.assertArrayEquals(expectedResult, actualResult);
        }












}

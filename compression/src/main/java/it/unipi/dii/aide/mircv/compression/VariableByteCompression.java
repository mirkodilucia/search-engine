package it.unipi.dii.aide.mircv.compression;

public class VariableByteCompression {
    // Method to encode an array of integers into a byte array using Variable Byte compression
    public static byte[] encode(int[] numbers) {
        // Allocate space for the compressed bytes (up to 5 bytes per input number)
        byte[] bytes = new byte[numbers.length * 5];
        int i = 0;

        // Iterate through each input number
        for (int number : numbers) {
            int j = 0;

            // Encode the number using Variable Byte compression
            while (number >= 128) {
                bytes[i] = (byte) (number % 128);
                number /= 128;
                bytes[i] += 128;
                i++;
                j++;
            }

            // Handle the last byte for the current number
            bytes[i] = (byte) number;
            i++;
        }

        // Create a result array with the actual size and copy the compressed bytes
        byte[] result = new byte[i];
        System.arraycopy(bytes, 0, result, 0, i);
        return result;
    }

    // Method to decode a byte array into an array of integers using Variable Byte compression
    public static int[] decode(byte[] bytes) {
        // Allocate space for the decoded numbers
        int[] numbers = new int[bytes.length];
        int i = 0;

        // Iterate through each byte in the input array
        for (byte b : bytes) {
            int number = 0;
            int j = 0;

            // Decode the byte using Variable Byte compression
            while (b < 0) {
                number += (b % 128) * Math.pow(128, j);
                b += 128;
                j++;
            }

            // Add the decoded number to the result array
            number += b * Math.pow(128, j);
            numbers[i] = number;
            i++;
        }

        // Create a result array with the actual size and copy the decoded numbers
        int[] result = new int[i];
        System.arraycopy(numbers, 0, result, 0, i);
        return result;
    }
}

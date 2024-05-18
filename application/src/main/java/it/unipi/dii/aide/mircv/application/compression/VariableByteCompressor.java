package it.unipi.dii.aide.mircv.application.compression;

public class VariableByteCompressor {
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
    public static int[] decode(byte[] bytes, int length) {
        int[] decompressedArray = new int[length];

        // integer that I'm processing
        int decompressedNumber = 0;

        // count of the processed numbers (used also as a pointer in the output array)
        int alreadyDecompressed = 0;

        for(byte elem: bytes){
            if((elem & 0xff) < 128)
                // not the termination byte, shift the actual number and insert the new byte
                decompressedNumber = 128 * decompressedNumber + elem;
            else{
                // termination byte, remove the 1 at the MSB and then append the byte to the number
                decompressedNumber = 128 * decompressedNumber + ((elem - 128) & 0xff);

                // save the number in the output array
                decompressedArray[alreadyDecompressed] = decompressedNumber;

                // increase the number of processed numbers
                alreadyDecompressed ++;

                //reset the variable for the next number to decompress
                decompressedNumber = 0;
            }
        }

        return decompressedArray;
    }
}

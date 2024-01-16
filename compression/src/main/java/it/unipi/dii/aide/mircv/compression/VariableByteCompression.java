package it.unipi.dii.aide.mircv.compression;

public class VariableByteCompression
{
public static byte[] encode(int[] numbers)
    {
        byte[] bytes = new byte[numbers.length * 5];
        int i = 0;
        for (int number : numbers)
        {
            int j = 0;
            while (number >= 128)
            {
                bytes[i] = (byte) (number % 128);
                number /= 128;
                bytes[i] += 128;
                i++;
                j++;
            }
            bytes[i] = (byte) number;
            i++;
        }
        byte[] result = new byte[i];
        System.arraycopy(bytes, 0, result, 0, i);
        return result;
    }

    public static int[] decode(byte[] bytes)
    {
        int[] numbers = new int[bytes.length];
        int i = 0;
        for (byte b : bytes)
        {
            int number = 0;
            int j = 0;
            while (b < 0)
            {
                number += (b % 128) * Math.pow(128, j);
                b += 128;
                j++;
            }
            number += b * Math.pow(128, j);
            numbers[i] = number;
            i++;
        }
        int[] result = new int[i];
        System.arraycopy(numbers, 0, result, 0, i);
        return result;
    }




}

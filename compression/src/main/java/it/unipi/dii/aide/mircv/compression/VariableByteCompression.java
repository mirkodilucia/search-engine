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
        int j = 0;
        int number = 0;
        while (i < bytes.length)
        {
            if (bytes[i] >= 0)
            {
                number += bytes[i];
                i++;
            }
            else
            {
                number += bytes[i] + 128;
                i++;
                numbers[j] = number;
                number = 0;
                j++;
            }
        }
        int[] result = new int[j];
        System.arraycopy(numbers, 0, result, 0, j);
        return result;
    }






}

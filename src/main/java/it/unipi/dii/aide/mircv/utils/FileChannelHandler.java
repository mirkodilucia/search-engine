package it.unipi.dii.aide.mircv.utils;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileChannelHandler {

    public static FileChannel open(String partialIndexDocsPath, StandardOpenOption...operations) throws IOException {
        return (FileChannel) Files.newByteChannel(Paths.get(partialIndexDocsPath), operations);
    }


}

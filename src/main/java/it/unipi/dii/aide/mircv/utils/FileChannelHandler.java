package it.unipi.dii.aide.mircv.utils;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileChannelHandler {

    /**
     * Open a FileChannel with the given path and operations
     * @param partialIndexDocsPath the path of the file
     * @param operations the operations to perform on the file
     * @return the FileChannel
     * @throws IOException if an I/O error occurs
     */
    public static FileChannel open(String partialIndexDocsPath, StandardOpenOption...operations) throws IOException {
        return (FileChannel) Files.newByteChannel(Paths.get(partialIndexDocsPath), operations);
    }


}

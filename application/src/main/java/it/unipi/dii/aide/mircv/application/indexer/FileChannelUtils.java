package it.unipi.dii.aide.mircv.application.indexer;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileChannelUtils {

    public static FileChannel openFileChannel(String partialIndexDocsPath, StandardOpenOption...operations) throws IOException {
        return (FileChannel) Files.newByteChannel(Paths.get(partialIndexDocsPath), operations);
    }

}

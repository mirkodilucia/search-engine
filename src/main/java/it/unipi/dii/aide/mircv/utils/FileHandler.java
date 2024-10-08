package it.unipi.dii.aide.mircv.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.channels.*;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;

public class FileHandler {

    /**
     * Reads a file and returns its content as a list of strings
     * @param path the path of the file
     */
    public static void createFileIfNotExists(String path) {
        File file = new File(path);
        try {
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a file and returns its content as a list of strings
     * @param fileName the path of the file
     * @param content the content to write
     */
    public static void writeStringToFile(String fileName, String content) throws IOException {
        try (FileChannel channel = FileChannel.open(
                new File(fileName).toPath(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            try (BufferedWriter writer = new BufferedWriter(Channels.newWriter(channel, StandardCharsets.UTF_8))) {
                writer.write(content);
            }
        }
    }

    /**
     * Reads a file and returns its content as a list of strings
     * @param fileName the path of the file
     */
    public static String readStringFromFile(String fileName) throws IOException {
        try (FileChannel channel = FileChannel.open(
                new File(fileName).toPath(),
                StandardOpenOption.READ)) {
            try (BufferedReader reader = new BufferedReader(Channels.newReader(channel, StandardCharsets.UTF_8))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return content.toString();
            }
        }
    }

    /**
     * Writes an object to a file
     * @param fileName the path of the file
     * @param object the object to write
     * @throws IOException
     */
    public static void writeObjectToFile(String fileName, Object object) throws IOException {
        try (FileChannel channel = FileChannel.open(
                new File(fileName).toPath(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            try (ObjectOutputStream oos = new ObjectOutputStream(Channels.newOutputStream(channel))) {
                oos.writeObject(object);
            }
        }
    }

    /**
     * Reads an object from a file
     * @param fileName
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readObjectFromFile(String fileName) throws IOException, ClassNotFoundException {
        try (FileChannel channel = FileChannel.open(
                new File(fileName).toPath(),
                StandardOpenOption.READ)) {
            try (ObjectInputStream ois = new ObjectInputStream(Channels.newInputStream(channel))) {
                return ois.readObject();
            }
        }
    }

    /**
     * Reads a file and returns its content as a list of strings
     * @param s the path of the file
     */
    public static boolean fileExists(String s) {
        return Files.exists(Paths.get(s));
    }

    /**
     * Reads a file and returns its content as a list of strings
     * @param s the path of the file
     */
    public static void createFolderIfNotExists(String s) {
        if (Files.exists(Paths.get(s))) {
            return;
        }

        try {
            Files.createDirectories(Paths.get(s));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a file and returns its content as a list of strings
     * @param stopwordPath the path of the file
     */
    public static HashMap<String, Integer> readStopwordLines(String stopwordPath) {
        HashMap<String, Integer> stopwords = new HashMap<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(stopwordPath), StandardCharsets.UTF_8)) {
            for (String line; (line = br.readLine()) != null; ) {
                if (line.isEmpty())
                    continue;

                //add word to stopwords list
                stopwords.put(line, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stopwords;
    }

    /**
     * Reads a file and returns its content as a list of strings
     * @param file the path of the file
     */
    public static void removeFile(String file) {
        try {
            Files.deleteIfExists(Paths.get(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a file and returns its content as a list of strings
     * @param pathToPartialVocabularies the path of the file
     */
    public static void deleteFile(String pathToPartialVocabularies) {
        // Delete all file matching pathToPartialVocabularies * with regex
        String[] dir = pathToPartialVocabularies.split("(?<=/)(?!.*?/)");
        if (dir.length == 0) { return; }
        try {
            Files.walk(Paths.get(dir[0]))
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            Files.deleteIfExists(file);
                        } catch (IOException e) {
                            System.out.println("File to delete not found: " + file);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a file and returns its content as a list of strings
     * @param partialIndexesDocumentsPath the path of the file
     */
    public static void deleteDirectory(String partialIndexesDocumentsPath) {
        try {
            Files.walk(Paths.get(partialIndexesDocumentsPath))
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

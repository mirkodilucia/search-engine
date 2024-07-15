package it.unipi.dii.aide.mircv.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.channels.*;
import java.util.Collection;
import java.util.ArrayList;

public class FileHandler {

    public static void createFileIfNotExists(String path) {
        File file = new File(path);
        try {
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeStringToFile(String fileName, String content) throws IOException {
        try (FileChannel channel = FileChannel.open(
                new File(fileName).toPath(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            try (BufferedWriter writer = new BufferedWriter(Channels.newWriter(channel, StandardCharsets.UTF_8))) {
                writer.write(content);
            }
        }
    }

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

    public static void writeObjectToFile(String fileName, Object object) throws IOException {
        try (FileChannel channel = FileChannel.open(
                new File(fileName).toPath(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            try (ObjectOutputStream oos = new ObjectOutputStream(Channels.newOutputStream(channel))) {
                oos.writeObject(object);
            }
        }
    }

    public static Object readObjectFromFile(String fileName) throws IOException, ClassNotFoundException {
        try (FileChannel channel = FileChannel.open(
                new File(fileName).toPath(),
                StandardOpenOption.READ)) {
            try (ObjectInputStream ois = new ObjectInputStream(Channels.newInputStream(channel))) {
                return ois.readObject();
            }
        }
    }

    public static boolean fileExists(String s) {
        return Files.exists(Paths.get(s));
    }

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

    public static Collection<String> readStopwordLines(String stopwordPath) {
        Collection<String> stopwords = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(stopwordPath), StandardCharsets.UTF_8)) {
            for (String line; (line = br.readLine()) != null; ) {
                if (line.isEmpty())
                    continue;

                //add word to stopwords list
                stopwords.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stopwords;
    }

    public static void removeFile(String file) {
        try {
            Files.deleteIfExists(Paths.get(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

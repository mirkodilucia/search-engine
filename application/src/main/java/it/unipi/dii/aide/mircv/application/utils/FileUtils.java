package it.unipi.dii.aide.mircv.application.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

public class FileUtils {

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

    public static String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeFile(String path, String data) {
        try {
            Files.write(Paths.get(path), data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeFile(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFolder(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createFolder(String folderPath) {
        try {
            Files.createDirectory(Paths.get(folderPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createFile(String path) {
        try {
            Files.createFile(Paths.get(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package it.unipi.dii.aide.mircv.utils;

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
}

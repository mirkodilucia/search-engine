package it.unipi.dii.aide.mircv.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    private static final String PATH_TO_STOPWORDS = "../resources/stopwords.dat";

    public static Collection<String> readStopwordLines() {
        Collection<String> stopwords = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(PATH_TO_STOPWORDS), StandardCharsets.UTF_8)) {
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
}

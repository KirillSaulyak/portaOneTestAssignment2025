package org.example.gram;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGram {
    private final List<String> gramWords;

    public AbstractGram(String gramFileName) {
        gramWords = new ArrayList<>();
        String filePath = "src/main/resources/".concat(gramFileName);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String line = bufferedReader.readLine(); // skip header
            while ((line = bufferedReader.readLine()) != null) {
                String[] gram = line.split(",\\d+");
                if (gram.length == 0) {
                    continue;
                }
                String word = gram[0].replaceAll("[\"']", "").trim();
                if (!word.isEmpty()) {
                    gramWords.add(word);
                    gramWords.sort((o1, o2) -> o2.length() - o1.length());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + filePath + " - " + e.getMessage());
        }
    }

    public List<String> getGramWords() {
        return gramWords;
    }
}

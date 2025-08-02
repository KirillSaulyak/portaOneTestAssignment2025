package org.example;

import org.example.gram.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class RepairText {
    private final AbstractGram oneGram = new OneGram();
    private final AbstractGram threeGram = new ThreeGram();

    private String unprocessedText;

    private String result = "";

    private List<String> repairedText = new ArrayList<>();

    private List<String> deletedPartUnprocessedText = new LinkedList<>();

    private String blockedText = "";

    public RepairText() {
        System.out.println("Enter text:");
        try (Scanner scanner = new Scanner(System.in)) {
            this.unprocessedText = scanner.nextLine().trim();
        }
    }

    public String repair() {
        buildSentence(oneGram.getGramWords(), threeGram.getGramWords());
        return getResult();

    }

    public String getResult() {
        return result;
    }

    private String getUnprocessedText() {
        return unprocessedText;
    }

    private void addDeletedPartUnprocessedText(String deletedPartUnprocessedText) {
        this.deletedPartUnprocessedText.addFirst(deletedPartUnprocessedText);
    }

    private List<String> getDeletedPartUnprocessedText() {
        return deletedPartUnprocessedText;
    }

    private void returnDeletedPartUnprocessedText() {
        this.unprocessedText = getDeletedPartUnprocessedText().getFirst().concat(getUnprocessedText());
    }

    private void deleteReturnedPartUnprocessedText() {
        this.deletedPartUnprocessedText.removeFirst();
    }

    private String getBlockedText() {
        return blockedText;
    }

    private void setBlockedText(String blockedText) {
        this.blockedText = blockedText;
    }

    private void clearBlockedText() {
        this.blockedText = "";
    }

    private void deleteFoundText(String foundText) {
        String[] words = foundText.split(" ");
        int lengthToDelete = 0;
        for (String word : words) {
            lengthToDelete += word.length();
        }

        addDeletedPartUnprocessedText(getUnprocessedText().substring(0, lengthToDelete));

        this.unprocessedText = getUnprocessedText().substring(lengthToDelete);
    }

    private List<String> getRepairedText() {
        return repairedText;
    }

    private void addRepairedText(String foundText) {
        this.repairedText.add(foundText);
    }

    private void deleteLastRepairedText() {
        repairedText.removeLast();
    }

    private void commitRepairedText(String repairedText) {
        addRepairedText(repairedText);
        deleteFoundText(repairedText);
    }

    private void rollback() {
        returnDeletedPartUnprocessedText();
        deleteReturnedPartUnprocessedText();
        deleteLastRepairedText();
    }

    private void makeResult() {
        this.result = getRepairedText().toString().replaceAll("[\\[\\],]", "").trim();
    }

    private void buildSentence(List<String> oneGramWords, List<String> threeGramWords) {

        String matchedThreeGrams;

        while (!getUnprocessedText().trim().isEmpty()) {
            String matchedText;

            matchedThreeGrams = findThreeGramMatches(threeGramWords, getUnprocessedText());
            if (!matchedThreeGrams.isEmpty()) {
                matchedText = matchedThreeGrams;
            } else {
                matchedText = findOneGramMatches(oneGramWords, getUnprocessedText());
            }

            if (!matchedText.isEmpty()) {
                if (!getBlockedText().isEmpty()) {
                    clearBlockedText();
                }
                commitRepairedText(matchedText);
            } else {
                setBlockedText(repairedText.getLast());
                rollback();
            }
        }
        makeResult();
    }

    private boolean isMatchedText(String segment, String text) {
        boolean maskMatch = true;
        boolean segmentContainsUppercase = segment.matches(".*[A-Z].*");
        boolean textContainsUppercase = text.matches(".*[A-Z].*");
        if (segmentContainsUppercase != textContainsUppercase) {
            return false;
        }

        for (int i = 0; i < segment.length(); i++) {
            if (segment.charAt(i) != '*' && segment.charAt(i) != text.charAt(i)) {
                maskMatch = false;
                break;
            }
        }
        if (maskMatch) {
            return true;
        }

        return isMatchesWordWithStars(segment, text);
    }

    private boolean isMatchesWordWithStars(String segment, String GramText) {
        int[] countSegmentWords = new int[26];
        int[] countGramWords = new int[26];
        int stars = 0;

        for (char c : segment.toCharArray()) {
            if (c == '*') {
                stars++;
            } else if (c >= 'a' && c <= 'z') {
                countSegmentWords[c - 'a']++;
            }
        }

        for (char c : GramText.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                countGramWords[c - 'a']++;
            }
        }

        int missing = 0;
        for (int i = 0; i < 26; i++) {
            if (countGramWords[i] > countSegmentWords[i]) {
                missing += countGramWords[i] - countSegmentWords[i];
            }
        }

        return missing == stars;
    }

    private String findOneGramMatches(List<String> oneGramWords, String brokenText) {
        for (String word : oneGramWords) {
            if (word.equals(getBlockedText())) {
                continue;
            }
            int wordLength = word.length();
            for (int i = 0; i <= brokenText.length() - wordLength; i++) {
                if (isMatchedText(brokenText.substring(0, (word.length())), word)) {
                    return word;
                }
            }
        }
        return "";
    }

    private String findThreeGramMatches(List<String> threeGramWords, String brokenText) {
        boolean firstWordFound;
        boolean secondWordFound;
        boolean thirdWordFound;

        int firstWordLength;
        int secondWordLength;
        int thirdWordLength;

        for (String threeGram : threeGramWords) {
            if (threeGram.equals(getBlockedText())) {
                continue;
            }
            if (brokenText != null) {
                String ngramWithoutSpaces = threeGram.replace(" ", "");
                if (ngramWithoutSpaces.length() > brokenText.length()) {
                    continue;
                }
                String[] words = threeGram.split(" ");

                firstWordLength = words[0].length();
                secondWordLength = words[1].length();
                thirdWordLength = words[2].length();

                firstWordFound = isMatchedText(brokenText.substring(0, (firstWordLength)), words[0]);
                secondWordFound = isMatchedText(brokenText.substring((firstWordLength), (firstWordLength + secondWordLength)), words[1]);
                thirdWordFound = isMatchedText(brokenText.substring((firstWordLength + secondWordLength), ((firstWordLength + secondWordLength) + thirdWordLength)), words[2]);
                if (firstWordFound && secondWordFound && thirdWordFound) {
                    return threeGram;
                }
            }
        }
        return "";
    }
}

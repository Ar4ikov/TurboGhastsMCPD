package com.happyghast.gai.data;

import java.util.*;

/**
 * Generates unique Russian-style license plate numbers.
 * Format: X000XX 52 where X are Cyrillic letters that resemble Latin ones.
 * Region code is always 52.
 */
public class PlateGenerator {

    private static final char[] PLATE_LETTERS = {
            '\u0410', // А
            '\u0412', // В
            '\u0415', // Е
            '\u041A', // К
            '\u041C', // М
            '\u041D', // Н
            '\u041E', // О
            '\u0420', // Р
            '\u0421', // С
            '\u0422', // Т
            '\u0423', // У
            '\u0425'  // Х
    };

    private static final String REGION = "52";
    private static final Random RANDOM = new Random();

    /**
     * Generates a unique plate number that doesn't exist in the given set.
     */
    public static String generateUnique(Set<String> existingPlates) {
        String plate;
        int attempts = 0;
        do {
            plate = generate();
            attempts++;
            if (attempts > 10000) {
                throw new RuntimeException("Cannot generate unique plate number after 10000 attempts");
            }
        } while (existingPlates.contains(plate));
        return plate;
    }

    /**
     * Generates several unique plate numbers for the player to choose from.
     */
    public static List<String> generateChoices(Set<String> existingPlates, int count) {
        Set<String> generated = new LinkedHashSet<>();
        int attempts = 0;
        while (generated.size() < count) {
            String plate = generate();
            attempts++;
            if (attempts > 10000) {
                break;
            }
            if (!existingPlates.contains(plate)) {
                generated.add(plate);
            }
        }
        return new ArrayList<>(generated);
    }

    /**
     * Validates that a plate string matches the format: Б000ББ 52
     * where Б is one of 12 allowed Cyrillic letters and 0 is a digit.
     */
    public static boolean isValidFormat(String plate) {
        if (plate == null) return false;
        String trimmed = plate.trim();
        if (!trimmed.endsWith(" " + REGION)) return false;
        String body = trimmed.substring(0, trimmed.length() - REGION.length() - 1);
        if (body.length() != 6) return false;
        if (!isPlateLetter(body.charAt(0))) return false;
        for (int i = 1; i <= 3; i++) {
            if (!Character.isDigit(body.charAt(i))) return false;
        }
        if (!isPlateLetter(body.charAt(4))) return false;
        if (!isPlateLetter(body.charAt(5))) return false;
        return true;
    }

    private static boolean isPlateLetter(char c) {
        for (char pl : PLATE_LETTERS) {
            if (pl == c) return true;
        }
        return false;
    }

    /**
     * Normalizes a user-typed plate: appends region if missing.
     */
    public static String normalize(String input) {
        if (input == null) return "";
        String trimmed = input.trim();
        if (!trimmed.endsWith(" " + REGION)) {
            trimmed = trimmed + " " + REGION;
        }
        return trimmed;
    }

    private static String generate() {
        char l1 = PLATE_LETTERS[RANDOM.nextInt(PLATE_LETTERS.length)];
        int digits = RANDOM.nextInt(1000);
        char l2 = PLATE_LETTERS[RANDOM.nextInt(PLATE_LETTERS.length)];
        char l3 = PLATE_LETTERS[RANDOM.nextInt(PLATE_LETTERS.length)];
        return String.format("%c%03d%c%c %s", l1, digits, l2, l3, REGION);
    }
}

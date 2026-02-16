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

    private static String generate() {
        char l1 = PLATE_LETTERS[RANDOM.nextInt(PLATE_LETTERS.length)];
        int digits = RANDOM.nextInt(1000);
        char l2 = PLATE_LETTERS[RANDOM.nextInt(PLATE_LETTERS.length)];
        char l3 = PLATE_LETTERS[RANDOM.nextInt(PLATE_LETTERS.length)];
        return String.format("%c%03d%c%c %s", l1, digits, l2, l3, REGION);
    }
}

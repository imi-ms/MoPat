package de.imi.mopat.utils;

import java.util.Random;

/**
 *
 */
public class Helper {

    private static final Random random = new Random();

    /**
     * Returns a random String with a given length
     *
     * @param length length of the String
     * @return a random String with a given length
     */
    public static String getRandomString(int length) {
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890 .,:;-_!§$%&/()=?*+~#";
        String returnString = "";
        for (int i = 0; i < length; i++) {
            returnString += alphabet.charAt(random.nextInt(alphabet.length()));
        }
        return returnString;
    }

    /**
     * Returns a random alphabetic lowercase String with a given length
     *
     * @param length length of the String
     * @return a random alphabetic lowercase String with a given length
     */
    public static String getRandomAlphabeticString(int length) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        String returnString = "";
        for (int i = 0; i < length; i++) {
            returnString += alphabet.charAt(random.nextInt(alphabet.length()));
        }
        return returnString;
    }

    /**
     * Returns a random alphanumeriric String with a given length
     *
     * @param length length of the String
     * @return a random alphanumeriric String with a given length
     */
    public static String getRandomAlphanumericString(int length) {
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String returnString = "";
        for (int i = 0; i < length; i++) {
            returnString += alphabet.charAt(random.nextInt(alphabet.length()));
        }
        return returnString;
    }

    /**
     * Returns a random locale-String with Format "xx_XX"
     *
     * @return a random locale-String
     */
    public static String getRandomLocale() {
        return getRandomAlphabeticString(2) + "_" + getRandomAlphabeticString(2).toUpperCase();
    }

    /**
     * Returns a random mail address
     *
     * @return a random mail address
     */
    public static String getRandomMailAddress() {
        return getRandomAlphanumericString(random.nextInt(50) + 1) + "@"
            + getRandomAlphabeticString(random.nextInt(5) + 5) + "." + getRandomAlphabeticString(2);
    }

    /**
     * Returns a random value of the given enum class
     *
     * @param <T>       Enum type
     * @param enumClass class of the enum
     * @return a random value of the given enum class
     */
    public static <T extends Enum<?>> T getRandomEnum(Class<T> enumClass) {
        int x = random.nextInt(enumClass.getEnumConstants().length);
        return enumClass.getEnumConstants()[x];
    }
}

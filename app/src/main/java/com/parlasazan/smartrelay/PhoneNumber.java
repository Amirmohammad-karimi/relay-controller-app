package com.parlasazan.smartrelay;

/** Normalizes phone numbers entered with Latin, Persian, or Arabic digits. */
public final class PhoneNumber {
    private PhoneNumber() {}

    public static String normalize(String value) {
        if (value == null) return "";
        StringBuilder result = new StringBuilder();
        String trimmed = value.trim();
        for (int i = 0; i < trimmed.length(); i++) {
            char character = trimmed.charAt(i);
            if (character == '+' && result.length() == 0) {
                result.append(character);
            } else if (character >= '0' && character <= '9') {
                result.append(character);
            } else if (character >= '\u06F0' && character <= '\u06F9') {
                result.append((char) ('0' + character - '\u06F0'));
            } else if (character >= '\u0660' && character <= '\u0669') {
                result.append((char) ('0' + character - '\u0660'));
            } else if (!Character.isWhitespace(character) && character != '-' && character != '(' && character != ')') {
                return "";
            }
        }
        return result.toString();
    }

    public static boolean isValid(String value) {
        return value != null && value.matches("\\+?\\d{7,15}");
    }
}

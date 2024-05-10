package org.pipeman.utils;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class Utils {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();

    public static Optional<Integer> parseInt(String s) {
        if (s == null) return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(ALPHABET.length());
            char randomChar = ALPHABET.charAt(index);
            sb.append(randomChar);
        }
        return sb.toString();
    }

    public static int getDay() {
        return (int) (System.currentTimeMillis() / 86_400_000);
    }

    public static <T> T tryThis(ThrowingSupplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] toStringArray(JSONArray input) {
        String[] out = new String[input.length()];
        for (int i = 0; i < input.length(); i++) out[i] = input.getString(i);
        return out;
    }

    public static String substring(String in, int end) {
        return in.substring(0, Math.min(in.length(), end));
    }

    public static String readResourceString(String fileName) {
        try (InputStream stream = Utils.class.getResourceAsStream("/" + fileName)) {
            return new String(Objects.requireNonNull(stream).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

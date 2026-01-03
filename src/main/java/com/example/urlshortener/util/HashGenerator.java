package com.example.urlshortener.util;

import java.security.SecureRandom;

public final class HashGenerator {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RAND = new SecureRandom();

    public static String randomCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) sb.append(ALPHABET.charAt(RAND.nextInt(ALPHABET.length())));
        return sb.toString();
    }

    private HashGenerator() {}
}
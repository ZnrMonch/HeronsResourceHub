package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class HashUtil {

    private HashUtil() {}


    public static String sha256(String input) {
        if (input == null || input.isBlank()) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.trim().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static String mask(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) return "—";
        return "##########"; // fixed 10 # for all payment fields
    }
}
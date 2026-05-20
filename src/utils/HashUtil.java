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

    // Shows first 2 chars then masks the rest with #
    public static String mask(String value) {
        if (value == null || value.isBlank()) return "—";
        if (value.length() <= 2) return value;
        String visible = value.substring(0, 2);
        String masked  = "#".repeat(value.length() - 2);
        return visible + masked;
    }

    // Shows first 1 char then masks the rest — for short values
    public static String maskOne(String value) {
        if (value == null || value.isBlank()) return "—";
        if (value.length() <= 1) return value;
        String visible = value.substring(0, 1);
        String masked  = "#".repeat(value.length() - 1);
        return visible + masked;
    }
}
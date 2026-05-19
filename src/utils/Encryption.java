package utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Encryption {

    private static final String SECRET_KEY = "UMakSecretKey123"; 
    private static final byte[] IV         = new byte[16];      

    private Encryption() {}

    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) return null;
        try {
            SecretKeySpec  keySpec  = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");
            IvParameterSpec ivSpec  = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return Base64.getEncoder().encodeToString(
                    cipher.doFinal(plainText.getBytes("UTF-8")));
        } catch (Exception e) {
            System.err.println("EncryptionUtil.encrypt() failed: " + e.getMessage());
            return null;
        }
    }

    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) return null;
        try {
            SecretKeySpec  keySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)), "UTF-8");
        } catch (Exception e) {
            // Bad/legacy data — return null so caller can fall back to raw value
            return null;
        }
    }
}

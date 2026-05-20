package utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Encryption {
	
	// FIELDS
	
	// A fixed secret text used to lock and unlock the data
	private static final String SECRET_KEY = "UMakSecretKey123";
	// A fixed array of 16 empty bytes used to start the locking math
	private static final byte[] IV = new byte[16];

	// CONSTRUCTORS

	// Empty block that is hidden so no one can create an object of this class
	private Encryption() {
	}

	// METHODS

	// Takes normal text and scrambles it into hidden text
	public static String encrypt(String plainText) {
		// Check if text is empty or missing
		if (plainText == null || plainText.isBlank())
			return null; // Stop and return nothing
		try {
			// Prepare the secret key for AES locking
			SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");
			// Prepare the starting bytes
			IvParameterSpec ivSpec = new IvParameterSpec(IV);
			// Get the AES locking tool
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			// Turn on the tool in lock mode
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
			// Scramble the text and turn it into a string
			return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes("UTF-8")));
		} catch (Exception e) {
			// Print error if locking fails
			System.err.println("EncryptionUtil.encrypt() failed: " + e.getMessage());
			return null; // Return nothing on error
		}
	}

	// Takes scrambled text and turns it back into normal readable text
	public static String decrypt(String cipherText) {
		if (cipherText == null || cipherText.isBlank())
			return null; 
		try {
			SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(IV);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
			return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)), "UTF-8");
		} catch (Exception e) {
			// FIX: If decryption fails, assume it's an old plain-text password 
            // and just return it as-is so the login can still work!
			return cipherText; 
		}
	}

	// Shortcut function that locks a password string
	public static String encryptPassword(String plainPassword) {
		// Calls the main lock function
		return encrypt(plainPassword);
	}

	// Shortcut function that unlocks a password string
	public static String decryptPassword(String encryptedPassword) {
		// Calls the main unlock function
		return decrypt(encryptedPassword);
	}
}
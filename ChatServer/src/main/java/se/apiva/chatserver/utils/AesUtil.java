package se.apiva.chatserver.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AesUtil {

    // AES requires a 16, 24 or 32 byte key
    private static final String SECRET_KEY = "MySuperSecretKey12345678901234!@";

    // Encrypt a plaintext message
    public static String encrypt(String plaintext) throws Exception {
        SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
        // Encode to Base64 so it can be stored as a readable string in the database
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decrypt an encrypted message
    public static String decrypt(String encrypted) throws Exception {
        SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        return new String(decryptedBytes);
    }
}
package util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class AESUtil {
    
    public static byte[] encrypt(byte[] K_e,byte[] plaintext) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
    	byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivps = new IvParameterSpec(iv);
    	
        SecretKeySpec secretKeySpec = new SecretKeySpec(K_e, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec,ivps);
        
        byte[] ciphertext = cipher.doFinal(plaintext);
        
        byte[] iv_ciphertext = Arrays.copyOf(iv, iv.length + ciphertext.length);
        System.arraycopy(ciphertext, 0, iv_ciphertext, iv.length, ciphertext.length);
        
        return iv_ciphertext;
    }

    public static byte[] decrypt(byte[] K_e,byte[] iv_ciphertext) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        byte[] iv = Arrays.copyOfRange(iv_ciphertext, 0, 16);
        byte[] ciphertext = Arrays.copyOfRange(iv_ciphertext, 16, iv_ciphertext.length);
    	
    	IvParameterSpec ivps = new IvParameterSpec(iv);

        SecretKeySpec secretKeySpec = new SecretKeySpec(K_e, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec,ivps);
        try {
            byte[] res = cipher.doFinal(ciphertext);
            return res;
        }catch (Exception e){

        }
        return  null;
    }

}
package util;

import javax.crypto.Mac;
import java.security.MessageDigest;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Hash {

    private static Random random = new Random();

    public static long hash64(long x, long seed) {
        x += seed;
        x = (x ^ (x >>> 33)) * 0xff51afd7ed558ccdL;
        x = (x ^ (x >>> 33)) * 0xc4ceb9fe1a85ec53L;
        x = x ^ (x >>> 33);
        return x;
    }

    public static int reduce(int hash, int n) {
        return (int) (((hash & 0xffffffffL) * n) >>> 32);
    }

    public static byte[]  Get_SHA_256(byte[] passwordToHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("Sha-256");
            md.update(passwordToHash);
            byte[] bytes = md.digest();
            return bytes;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static byte[] Get_HMAC_SHA_256(byte[] key, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException {

        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
        hmacSha256.init(secretKeySpec);

        byte[] hmacBytes = hmacSha256.doFinal(data);
        return hmacBytes;
    }

    public static byte[]  Get_MD5(byte[] passwordToHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(passwordToHash);
            byte[] bytes = md.digest();
            return bytes;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static byte[]  Get_Sha_128(byte[] passwordToHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("Sha-256");
            md.update(passwordToHash);
            byte[] bytes = md.digest();
            byte[] hash_128 = new byte[16];
            System.arraycopy(bytes,0,hash_128,0,16);
            return hash_128;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}

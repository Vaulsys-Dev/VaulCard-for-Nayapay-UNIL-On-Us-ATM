package vaulsys.util;

import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created by HP on 26-Jun-18.
 */
public class WSEncrptionUtil {
    private static final Logger logger = Logger.getLogger(WSEncrptionUtil.class);
    private static SecretKeySpec secretKey;
    private static byte[] key;


    public static byte[] DESEncrypt(byte[] message, String Key) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest(Key.getBytes("utf-8"));
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        for (int j = 0, k = 16; j < 8;) {
            keyBytes[k++] = keyBytes[j++];
        }

        final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        final Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        return cipher.doFinal(message);
    }

    public static byte[] DESDecrypt(byte[] message, String Key) throws Exception {

            final MessageDigest md = MessageDigest.getInstance("md5");
            final byte[] digestOfPassword = md.digest(Key.getBytes("utf-8"));
            final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            for (int j = 0, k = 16; j < 8; ) {
                keyBytes[k++] = keyBytes[j++];
            }

            final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            final Cipher decipher = Cipher.getInstance("DESede/CBC/NoPadding");
            decipher.init(Cipher.DECRYPT_MODE, key, iv);

            return decipher.doFinal(message);
    }

    public static String AESEncrypt(String strToEncrypt, String secret)
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        }
        catch (Exception e)
        {
            logger.error("Error while encrypting AES: " + e.toString());
        }
        return null;
    }

    public static void setKey(String myKey)
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String AESDecrypt(String strToDecrypt, String secret)
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Error while decrypting AES: " + e.toString());
        }
        return null;
    }

    public static void GenerateSHA1RandomKeyandData() //Raza TODO use this from UI
    {
        try {
            logger.info("Generating SHA-1 based Random Key and Data....");

            SecureRandom secureRandomKey = SecureRandom.getInstanceStrong();
            byte[] Keybytes = new byte[512];
            secureRandomKey.nextBytes(Keybytes);

            Long keyValue = Integer.toUnsignedLong(secureRandomKey.nextInt());
            String KEY = getSHA1Encryption(keyValue.toString());

            System.out.println("Random Key [" + KEY + "]"); //Not putting in Log for Security

            secureRandomKey.nextBytes(Keybytes);

            keyValue = Integer.toUnsignedLong(secureRandomKey.nextInt());
            String DATA = getSHA1Encryption(keyValue.toString());

            System.out.println("Random Data [" + DATA + "]"); //Not putting in Log for Security

            logger.info("Random Key and Data generation completed OK!");
        }
        catch (Exception e)
        {
            logger.error("Exception caught while generating SHA-1 Random Key and Data");
            e.printStackTrace();
        }
    }

    public static void GenerateSHA256RandomKeyandData() //Raza TODO use this from UI
    {
        try {
            logger.info("Generating SHA-256 based Random Key and Data....");

            SecureRandom secureRandomKey = SecureRandom.getInstanceStrong();
            byte[] Keybytes = new byte[256];
            secureRandomKey.nextBytes(Keybytes);

            Long keyValue = Integer.toUnsignedLong(secureRandomKey.nextInt());
            String key = getSHA256Encryption(keyValue.toString());

            System.out.println("Random Key [" + key + "]"); //Not putting in Log for Security


            SecureRandom secureRandomKey2 = SecureRandom.getInstanceStrong();
            secureRandomKey2.nextBytes(Keybytes);

            keyValue = Integer.toUnsignedLong(secureRandomKey2.nextInt());
            String data = getSHA256Encryption(keyValue.toString());

            System.out.println("Random Data [" + data + "]"); //Not putting in Log for Security

            logger.info("Random Key and Data generation completed OK!");
        }
        catch (Exception e)
        {
            logger.error("Exception caught while generating SHA-256 Random Key and Data");
            e.printStackTrace();
        }
    }

    public static void GenerateSHA256RandomKeyandDataWithFile(String KeyFileNamewitPath, String DataFileNamewitPath)
    {
        try {
            logger.info("Generating SHA-256 based Random Key and Data....");

            SecureRandom secureRandomKey = SecureRandom.getInstanceStrong();
            byte[] Keybytes = new byte[256];
            secureRandomKey.nextBytes(Keybytes);

            Long keyValue = Integer.toUnsignedLong(secureRandomKey.nextInt());
            String key = getSHA256Encryption(keyValue.toString());

            System.out.println("Random Key [" + key + "]");

            //String keyFile = "C:\\Users\\HP\\Desktop\\aeskey.txt";
            FileOutputStream out = new FileOutputStream(KeyFileNamewitPath);
            SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
            byte[] keyb = skey.getEncoded();
            out.write(keyb);
            out.close();

            SecureRandom secureRandomKey2 = SecureRandom.getInstanceStrong();
            secureRandomKey2.nextBytes(Keybytes);

            keyValue = Integer.toUnsignedLong(secureRandomKey2.nextInt());
            String data = getSHA256Encryption(keyValue.toString());

            System.out.println("Random Data [" + data + "]"); //Not putting in Log for Security

            out = new FileOutputStream(DataFileNamewitPath);
            byte[] datab = data.getBytes();
            out.write(datab);
            out.close();

            logger.info("Random Key and Data generation completed OK!");
        }
        catch (Exception e)
        {
            logger.error("Exception caught while generating SHA-256 Random Key and Data");
            e.printStackTrace();
        }
    }

    public static String getSHA1Encryption(String input) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(input.getBytes("UTF-8"));

        return new BigInteger(1, crypt.digest()).toString(16);
    }

    public static String getSHA256Encryption(String input) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-256");
        crypt.reset();
        crypt.update(input.getBytes("UTF-8"));

        return new BigInteger(1, crypt.digest()).toString(16);
    }

    public static byte[] AES256Encrypt(String plainText, String key) throws Exception {
        byte[] clean = plainText.getBytes();

        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Hashing key.
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(key.getBytes("UTF-8"));

        byte[] keyBytes = new byte[16];
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Encrypt.
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(clean);

        // Combine IV and encrypted part.
        byte[] encryptedIVAndText = new byte[ivSize + encrypted.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, ivSize);
        System.arraycopy(encrypted, 0, encryptedIVAndText, ivSize, encrypted.length);

        return encryptedIVAndText;
    }

    public static String AES256Decrypt(byte[] encryptedIvTextBytes, String key) throws Exception {
        int ivSize = 16;
        int keySize = 16;

        // Extract IV.
        byte[] iv = new byte[ivSize];
        System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Extract encrypted part.
        int encryptedSize = encryptedIvTextBytes.length - ivSize;
        byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize);

        // Hash key.
        byte[] keyBytes = new byte[keySize];
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(key.getBytes());
        System.arraycopy(md.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Decrypt.
        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);

        return new String(decrypted);
    }

    public static String AES256GCMEncryptWithoutVector(String plainText, String key) throws Exception {

        // Hashing key.
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(key.getBytes("UTF-8"));

        byte[] keyBytes = new byte[16];
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Encrypt.
        Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding", "SunJCE");

        byte[] iv = new byte[128];
        for (int i=0; i<128; i++)
            iv[i] = 0;
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, spec);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes("UTF-8")));
    }

    public static String AES256GCMDecryptWithoutVector(String encryptedText, String key) throws Exception {

        // Hash key.
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(key.getBytes("UTF-8"));

        byte[] keyBytes = new byte[16];
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Decrypt.
        Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding", "SunJCE");

        byte[] iv = new byte[128];
        for (int i=0; i<128; i++)
            iv[i] = 0;
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));
    }

}

package vaulsys.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;


/**
 * Created by HP on 4/3/2017.
 */
public class Encryptor {
    final static String defaultKey = "FaNaPvAuLsYs";

    // String to hold the name of the private key file
    public static final String PRIVATE_KEY_FILE = "C:/keys/private.key";

    //String to hold name of the public key file.
    public static final String PUBLIC_KEY_FILE = "C:/keys/public.key";

    public static void main(String[] args) throws Exception {
        try {

            String key = "4B6250655368566D597133743677397A244226452948404D635166546A576E5A";
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please enter IP/Domain: ");
            String ip = scanner.next();

            System.out.println("Please enter Username: ");
            String username = scanner.next();

            System.out.println("Please enter Password: ");
            String password = scanner.next();

            System.out.println("Please enter SID: ");
            String sid = scanner.next();

            System.out.println("Please enter Schema: ");
            String schema = scanner.next();

            ip = "jdbc:oracle:thin:@" + ip + ":1521:" + sid;
            System.out.println("Encrypted URL => " + AES256GCMEncryptWithoutVector(ip, key));
            System.out.println("Encrypted Username => " + AES256GCMEncryptWithoutVector(username, key));
            System.out.println("Encrypted Password => " + AES256GCMEncryptWithoutVector(password, key));
            System.out.println("Encrypted Schema => " + AES256GCMEncryptWithoutVector(schema, key));

        }
        catch (NoSuchAlgorithmException noSuchAlgo)
        {
            System.out.println(" No Such Algorithm exists " + noSuchAlgo);
        }
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

    public static String getSHA1Encryption(String input) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(input.getBytes("UTF-8"));

        byte[] temp = crypt.digest();

        //System.out.println("Byte Value : " + new String(HexBin.encode(temp)));

        return new BigInteger(1, temp).toString(16);
    }

    private static SecretKeySpec secretKey;
    private static byte[] key;

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

    public static String encryptToAES(String strToEncrypt, String secret)
    {
        try
        {
//            System.out.println("Calling set for Encryption");
            setKey(secret);
//            System.out.println("Calling set for Encryption Done");
//            System.out.println("String to Encrypt [" + strToEncrypt + "]");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//            System.out.println("EnCrypted String [" + Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8"))) + "]");
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decryptToAES(String strToDecrypt, String secret)
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
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    public static byte[] DESEncrypt(byte[] message, String Key) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest(Key.getBytes("utf-8"));
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        for (int j = 0, k = 16; j < 8;) {
            keyBytes[k++] = keyBytes[j++];
        }

        final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        final Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);//, iv);

        return cipher.doFinal(message);
    }

    public static byte[] DESDecrypt(byte[] message, String Key) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest(Key.getBytes("utf-8"));
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        for (int j = 0, k = 16; j < 8;) {
            keyBytes[k++] = keyBytes[j++];
        }

        final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        final Cipher decipher = Cipher.getInstance("DESede/CBC/NoPadding");
        decipher.init(Cipher.DECRYPT_MODE, key);//, iv);

        return decipher.doFinal(message);
    }

    public static void checkRSAKeys()
    {
        RSAEncryptionUtil RSA_obj = new RSAEncryptionUtil();
        try {

            // Check if the pair of keys are present else generate those.
            if (!RSA_obj.areKeysPresent()) {
                // Method generates a pair of keys using the RSA algorithm and stores it in their respective files
                RSA_obj.generateKey();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] RSAEncrypt(String publicKeyFile, String originalText) throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = null;
        RSAEncryptionUtil RSA_obj = new RSAEncryptionUtil();

        // Encrypt the string using the public key
        inputStream = new ObjectInputStream(new FileInputStream(publicKeyFile));
        final PublicKey publicKey = (PublicKey) inputStream.readObject();
        final byte[] cipherText = RSA_obj.encrypt(originalText, publicKey);

        return cipherText;
    }

    public static byte[] RSAEncryptByKey(String publicKeyStr, String originalText) throws IOException, ClassNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException {

        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyStr));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);

        RSAEncryptionUtil RSA_obj = new RSAEncryptionUtil();

        // Encrypt the string using the public key
        final byte[] cipherText = RSA_obj.encrypt(originalText, publicKey);

        return cipherText;
    }

    public static String RSADecrypt(String privateKeyFile, byte[] cipherText) throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = null;
        RSAEncryptionUtil RSA_obj = new RSAEncryptionUtil();

        // Decrypt the cipher text using the private key.
        inputStream = new ObjectInputStream(new FileInputStream(privateKeyFile));
        final PrivateKey privateKey = (PrivateKey) inputStream.readObject();
        final String plainText = RSA_obj.decrypt(cipherText, privateKey);

        return plainText;
    }

    public static String RSADecryptByKey(String privateKeyStr, byte[] cipherText) throws IOException, ClassNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException {

        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(privateKeyStr));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKeyKey = keyFactory.generatePrivate(pubKeySpec);

        RSAEncryptionUtil RSA_obj = new RSAEncryptionUtil();

        // Decrypt the bytes using the private key
        final String plainText = RSA_obj.decrypt(cipherText, privateKeyKey);

        return plainText;
    }

    public static String stringToHex(String base)
    {
        StringBuffer buffer = new StringBuffer();
        int intValue;
        for(int x = 0; x < base.length(); x++)
        {
            int cursor = 0;
            intValue = base.charAt(x);
            String binaryChar = new String(Integer.toBinaryString(base.charAt(x)));
            for(int i = 0; i < binaryChar.length(); i++)
            {
                if(binaryChar.charAt(i) == '1')
                {
                    cursor += 1;
                }
            }
            if((cursor % 2) > 0)
            {
                intValue += 128;
            }
            buffer.append(Integer.toHexString(intValue) + "");
        }
        return buffer.toString();
    }

    public static byte[] hex2Byte(String str)
    {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++)
        {
            bytes[i] = (byte) Integer
                    .parseInt(str.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}

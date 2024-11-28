package vaulsys.reports;

import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;

import org.bouncycastle.bcpg.*;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.bcpg.sig.PreferredAlgorithms;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.test.FixedSecureRandom;
import org.bouncycastle.util.test.UncloseableOutputStream;

/**
 * Created by HP on 9/19/2017.
 */
public class PGPEncryption {

    private static PGPKeyRingGenerator rsaKr;
    private static PGPKeyRingGenerator dsaKr;
    private static char[] pwd = "vaulsys1234".toCharArray();
    private static int secpar = 2048;
    static int pwdCount = 100;

    public String encryptDecrypt(String data, String reportPublicKeyLocation) throws FileNotFoundException {

        //String encryptedData = "";
        byte[] encryptedData = null;

        try {

            genRsaKeyRing("www.vaulsysae.com", pwd);
            writeKeys();

            // init the security provider
            Security.addProvider(new BouncyCastleProvider());
            // read the key
            PGPPublicKey key = readPublicKey(reportPublicKeyLocation);
            //encrypt
            encryptedData = encryptData(data, key);

            // decrypt for verification
            String decrypt1 = decrypt(encryptedData, rsaKr.generateSecretKeyRing(), pwd);
            String decrypt2 = decrypt(encodeBase64(encryptedData), rsaKr.generateSecretKeyRing(), pwd);
            if (decrypt1.equals(decrypt2) && decrypt1.equals(data)){
                System.out.println("Successful PGP enc / dec");
            } else {
                System.out.println("Something went wrong in the PGP enc / dec");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //return encryptedData.toString();
        return encodeBase64(encryptedData);
    }

    private PGPPublicKey readPublicKey(String publicKeyFilePath) throws IOException, PGPException {
        try {
            InputStream in = new FileInputStream(new File(publicKeyFilePath));

            in = PGPUtil.getDecoderStream(in);
            PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in, new JcaKeyFingerprintCalculator());
            PGPPublicKey key = null;

            Iterator rIt = pgpPub.getKeyRings();
            while (key == null && rIt.hasNext()) {
                PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
                Iterator kIt = kRing.getPublicKeys();
                boolean encryptionKeyFound = false;

                while (key == null && kIt.hasNext()) {
                    PGPPublicKey k = (PGPPublicKey) kIt.next();
                    if (k.isEncryptionKey()) {
                        key = k;
                    }
                }
            }

            if (key == null) {
                throw new IllegalArgumentException(
                        "Can't find encryption key in key ring.");
            }

            return key;
        } catch (IOException io) {
            System.out.println("readPublicKey() threw an IOException");
            System.out.println(io.toString());
            throw io;
        }

    }

    private byte[] encryptData(String data, PGPPublicKey encKey)
            throws IOException, NoSuchProviderException, PGPException {

        try {
            byte[] dataInBytes = data.getBytes("UTF-8");

            PGPPublicKeyRing publicKeyRing = rsaKr.generatePublicKeyRing();
            Iterator<PGPPublicKey> pks = publicKeyRing.getPublicKeys();
            //PGPPublicKey encKey = null;
            int[] preferredSymmetricAlgorithms = null, preferredHashAlgorithms = null;

            // get the first encryption key
            while(pks.hasNext()){
                PGPPublicKey pk = pks.next();
                if (pk.isEncryptionKey()){
                    encKey = pk;
                    break;
                }
                // get preferred symmetric algorithm
                if (pk.isMasterKey()){
                    @SuppressWarnings("rawtypes")
                    Iterator v = pk.getSignatures();
                    while (v.hasNext()) {
                        PGPSignature sig = (PGPSignature)v.next();
                        PGPSignatureSubpacketVector hashedSubPackets = sig.getHashedSubPackets();
                        preferredSymmetricAlgorithms = getPreferredSymmetricAlgorithms(hashedSubPackets);
                        preferredHashAlgorithms = getPreferredHashAlgorithms(hashedSubPackets);
                    }
                }
            }

            // object that encrypts the data
            int preferredSymAlgo = PGPEncryptedData.AES_256;
            if (preferredSymmetricAlgorithms != null && preferredSymmetricAlgorithms.length != 0)
                preferredSymAlgo = preferredSymmetricAlgorithms[0];

            BcPGPDataEncryptorBuilder encryptorBuilder = new BcPGPDataEncryptorBuilder(preferredSymAlgo);
            encryptorBuilder.setWithIntegrityPacket(true);
            //encryptorBuilder.setSecureRandom(new SecureRandom());

            PGPEncryptedDataGenerator dataGenerator = new PGPEncryptedDataGenerator(encryptorBuilder);
            dataGenerator.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(encKey));

            // write the plain text bytes to the armored outputstream
            ByteArrayOutputStream baOut = new ByteArrayOutputStream();
            OutputStream cOut = dataGenerator.open(new UncloseableOutputStream(baOut), dataInBytes.length);
            cOut.write(dataInBytes);
            cOut.close();

            //return baOut.toString();
            return baOut.toByteArray();
        } catch (PGPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int[] getPreferredSymmetricAlgorithms(PGPSignatureSubpacketVector attributes) {
        SignatureSubpacket p = attributes.getSubpacket(SignatureSubpacketTags.PREFERRED_SYM_ALGS);

        if (p == null) {
            return null;
        }

        return ((PreferredAlgorithms) p).getPreferences();
    }

    public int[] getPreferredHashAlgorithms(PGPSignatureSubpacketVector attributes) {
        SignatureSubpacket p = attributes.getSubpacket(SignatureSubpacketTags.PREFERRED_HASH_ALGS);

        if (p == null) {
            return null;
        }

        return ((PreferredAlgorithms) p).getPreferences();
    }

    private String decrypt(String in, PGPSecretKeyRing decKeyRing, char[] pass){
        return decrypt(decodeBase64(in), decKeyRing, pass);
    }

    private String decrypt(byte[] in, PGPSecretKeyRing decKeyRing, char[] pass){
        try {
            PGPObjectFactory pgpF = new PGPObjectFactory(in, new JcaKeyFingerprintCalculator());
            PGPEncryptedDataList encList = (PGPEncryptedDataList)pgpF.nextObject();
            PGPPublicKeyEncryptedData encP = (PGPPublicKeyEncryptedData)encList.get(0);

            BcPBESecretKeyDecryptorBuilder decryptorBuilder = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider());
            PGPPrivateKey pgpPrivKey = decKeyRing.getSecretKey(encP.getKeyID()).extractPrivateKey(decryptorBuilder.build(pass));

            InputStream text = encP.getDataStream(new BcPublicKeyDataDecryptorFactory(pgpPrivKey));

            return streamToString(text);
        } catch (IOException e){
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String streamToString(InputStream in) {
        try {
            int ch;
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            while ((ch = in.read()) >= 0) {
                bOut.write(ch);
            }

            return new String(bOut.toByteArray());
        } catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    private String encodeBase64(byte[] bytes){
        return new String(Base64.encode(bytes));
    }

    private byte[] decodeBase64(String s){
        return Base64.decode(s);
    }

    public void writeKeys(){
        try{
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("pgpPublicKeyRing.pkr"));
            rsaKr.generatePublicKeyRing().encode(out);
            out.close();

            out = new BufferedOutputStream(new FileOutputStream("pgpSecretKeyRing.skr"));
            rsaKr.generateSecretKeyRing().encode(out);
            out.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void genRsaKeyRing(String id, char[] pass) {
        try {
            RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();

            // RSA KeyGen parameters
            BigInteger publicExponent = BigInteger.valueOf(0x10001);
            int certainty = 12;
            RSAKeyGenerationParameters rsaKeyGenerationParameters = new RSAKeyGenerationParameters(publicExponent, new SecureRandom(), secpar, certainty);
            kpg.init(rsaKeyGenerationParameters);

            // generate master key (signing) and subkey (enc)
            Date now = new Date();
            PGPKeyPair kpSign = new BcPGPKeyPair(PGPPublicKey.RSA_SIGN, kpg.generateKeyPair(), now);
            PGPKeyPair kpEnc = new BcPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, kpg.generateKeyPair(), now);

            // sign the master key packet
            PGPSignatureSubpacketGenerator signSigPacket = new PGPSignatureSubpacketGenerator();

            // metadata for master key
            boolean isCritical = true;
            int keyPurpose = KeyFlags.SIGN_DATA | KeyFlags.CERTIFY_OTHER;
            signSigPacket.setKeyFlags(isCritical, keyPurpose);

            int[] symAlgos = new int[] {SymmetricKeyAlgorithmTags.AES_256, SymmetricKeyAlgorithmTags.AES_192, SymmetricKeyAlgorithmTags.BLOWFISH, SymmetricKeyAlgorithmTags.CAST5};
            signSigPacket.setPreferredSymmetricAlgorithms(isCritical, symAlgos);

            int[] hashAlgos = new int[] {HashAlgorithmTags.SHA512, HashAlgorithmTags.SHA384, HashAlgorithmTags.SHA256};
            signSigPacket.setPreferredHashAlgorithms(isCritical, hashAlgos);

            signSigPacket.setFeature(isCritical, Features.FEATURE_MODIFICATION_DETECTION);

            // sign encryption subkey
            PGPSignatureSubpacketGenerator signEncPacket = new PGPSignatureSubpacketGenerator();

            // metadata for subkey
            keyPurpose = KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE;
            signEncPacket.setKeyFlags(isCritical, keyPurpose);


            // digests
            PGPDigestCalculator digest1 = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA1);
            PGPDigestCalculator digest256 = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA256);

            // encryption for secret key
            PBESecretKeyEncryptor pske = (new BcPBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, digest256, pwdCount)).build(pass);

            //  create the keyring
            BcPGPContentSignerBuilder contentSignerBuilder = new BcPGPContentSignerBuilder(kpSign.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256);
            rsaKr = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, kpSign, id, digest1, signSigPacket.generate(), null, contentSignerBuilder, pske);

            // encryption subkey
            rsaKr.addSubKey(kpEnc, signEncPacket.generate(), null);
        } catch (PGPException e){
            e.printStackTrace();
        }
    }
}

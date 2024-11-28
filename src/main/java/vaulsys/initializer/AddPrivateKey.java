package vaulsys.initializer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;
import javax.security.auth.x500.X500PrivateCredential;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

public class AddPrivateKey {

	public static void main(String[] args) throws Exception
	{
        Provider provider = (Provider)Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider").newInstance();
        Security.addProvider(provider);

        PrivateKey privKey = getPrivateKey();

        String keyStoreName="testconfig/LMK.jceks"; 
		char[] password = "$3cureP@$$".toCharArray();

		KeyStore store = addPrivateKeyToKeyStore(keyStoreName, privKey, password);

		OutputStream bOut = new FileOutputStream(keyStoreName);
		store.store(bOut, password);
		
		System.out.println("Process done succefully...");
	}
	
    public static PrivateKey getPrivateKey() throws Exception{
		KeyStore store = KeyStore.getInstance("JCEKS");
		char[] password = "tHiS!s#aPa$$w0&F@r#nCr*ps!0N".toCharArray();

		InputStream is = new FileInputStream("testconfig/private.key");
		
	    store.load(is, password);

	    PrivateKey priv = (PrivateKey) store.getKey("private-key", password);
	    return priv;
	}

	public static KeyStore addPrivateKeyToKeyStore(String keyStoreName, PrivateKey privKey, char[] password) throws Exception {
		KeyStore store = KeyStore.getInstance("JCEKS");

		InputStream is = new FileInputStream(keyStoreName);
		
	    store.load(is, password);
		
		X500PrivateCredential rootCredential = createRootCredential();
		X500PrivateCredential interCredential = createIntermediateCredential(rootCredential.getPrivateKey(),
				rootCredential.getCertificate());
		X500PrivateCredential endCredential = createEndEntityCredential(interCredential.getPrivateKey(),
				interCredential.getCertificate());

		Certificate[] chain = new Certificate[3];

		chain[0] = endCredential.getCertificate();
		chain[1] = interCredential.getCertificate();
		chain[2] = rootCredential.getCertificate();
//		chain[0] = rootCredential.getCertificate();

		
		store.setEntry("private-key", new KeyStore.PrivateKeyEntry(privKey, chain), new KeyStore.PasswordProtection(
				password));
		return store;
	}
	
	public static String ROOT_ALIAS = "root";
	public static String INTERMEDIATE_ALIAS = "intermediate";
	public static String END_ENTITY_ALIAS = "end";

    private static final int VALIDITY_PERIOD = 10 * 365 * 24 * 60 * 60 * 1000; // 10 years

	public static KeyPair generateRSAKeyPair() throws Exception {
		KeyPairGenerator kpGen;
		kpGen = KeyPairGenerator.getInstance("RSA", "BC");
		kpGen.initialize(1024, new SecureRandom());

		KeyPair keyPair = kpGen.generateKeyPair();

		return keyPair;
	}

	/**
	 * Generate a X500PrivateCredential for the root entity.
	 */
	public static X500PrivateCredential createRootCredential() throws Exception {
		KeyPair rootPair = generateRSAKeyPair();
		X509Certificate rootCert = generateRootCert(rootPair);

		return new X500PrivateCredential(rootCert, rootPair.getPrivate(), ROOT_ALIAS);
	}

	/**
	 * Generate a X500PrivateCredential for the intermediate entity.
	 */
	public static X500PrivateCredential createIntermediateCredential(PrivateKey caKey, X509Certificate caCert)
			throws Exception {
		KeyPair interPair = generateRSAKeyPair();
		X509Certificate interCert = generateIntermediateCert(interPair.getPublic(), caKey, caCert);

		return new X500PrivateCredential(interCert, interPair.getPrivate(), INTERMEDIATE_ALIAS);
	}

	/**
	 * Generate a X500PrivateCredential for the end entity.
	 */
	public static X500PrivateCredential createEndEntityCredential(PrivateKey caKey, X509Certificate caCert)
			throws Exception {
		KeyPair endPair = generateRSAKeyPair();
		X509Certificate endCert = generateEndEntityCert(endPair.getPublic(), caKey, caCert);

		return new X500PrivateCredential(endCert, endPair.getPrivate(), END_ENTITY_ALIAS);
	}

	public static X509Certificate generateRootCert(KeyPair pair) throws Exception {
		X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(1));
		certGen.setIssuerDN(new X500Principal("CN=Test CA Certificate"));
		certGen.setNotBefore(new Date(System.currentTimeMillis()));
		certGen.setNotAfter(new Date(System.currentTimeMillis() + VALIDITY_PERIOD));
		certGen.setSubjectDN(new X500Principal("CN=Test CA Certificate"));
		certGen.setPublicKey(pair.getPublic());
		certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

		return certGen.generateX509Certificate(pair.getPrivate(), "BC");
	}

	/**
	 * Generate a sample V3 certificate to use as an intermediate CA certificate
	 */
	public static X509Certificate generateIntermediateCert(PublicKey intKey, PrivateKey caKey, X509Certificate caCert)
			throws Exception {
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(1));
		certGen.setIssuerDN(caCert.getSubjectX500Principal());
		certGen.setNotBefore(new Date(System.currentTimeMillis()));
		certGen.setNotAfter(new Date(System.currentTimeMillis() + VALIDITY_PERIOD));
		certGen.setSubjectDN(new X500Principal("CN=Test Intermediate Certificate"));
		certGen.setPublicKey(intKey);
		certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

		certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
		certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(intKey));
		certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(0));
		certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature
				| KeyUsage.keyCertSign | KeyUsage.cRLSign));

		return certGen.generateX509Certificate(caKey, "BC");
	}

	/**
	 * Generate a sample V3 certificate to use as an end entity certificate
	 */
	public static X509Certificate generateEndEntityCert(PublicKey entityKey, PrivateKey caKey, X509Certificate caCert)
			throws Exception {
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(1));
		certGen.setIssuerDN(caCert.getSubjectX500Principal());
		certGen.setNotBefore(new Date(System.currentTimeMillis()));
		certGen.setNotAfter(new Date(System.currentTimeMillis() + VALIDITY_PERIOD));
		certGen.setSubjectDN(new X500Principal("CN=Test End Certificate"));
		certGen.setPublicKey(entityKey);
		certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

		certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
		certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(entityKey));
		certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
		certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature
				| KeyUsage.keyEncipherment));

		return certGen.generateX509Certificate(caKey, "BC");
	}

}

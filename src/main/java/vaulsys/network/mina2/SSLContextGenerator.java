package vaulsys.network.mina2;

import java.io.File;
import java.security.KeyStore;
import java.util.Arrays;

import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;
import org.apache.mina.filter.ssl.KeyStoreFactory;
import org.apache.mina.filter.ssl.SslContextFactory;
import org.apache.mina.filter.ssl.SslFilter;

public class SSLContextGenerator {
	private static final Logger logger = Logger.getLogger(SSLContextGenerator.class);

	private static SSLContext getSslContext() {
		SSLContext context = null;
		try {
			File key = new File(SSLContextGenerator.class.getResource("/ssl/keystore.jks").toURI());
			//File key = new File("/config/ssl/keystore.jks");
			logger.info("KeyStore File: " + key.getAbsolutePath());
			File trust = new File(SSLContextGenerator.class.getResource("/ssl/truststore.jks").toURI());
			//File trust = new File("/config/ssl/truststore.jks");
			logger.info("TrustStore File: " + trust.getAbsolutePath());

			KeyStoreFactory keyStoreFactory = new KeyStoreFactory();
			keyStoreFactory.setDataFile(key);
			keyStoreFactory.setPassword("qazwsx@123");

			KeyStoreFactory trustStoreFactory = new KeyStoreFactory();
			trustStoreFactory.setDataFile(trust);
			trustStoreFactory.setPassword("qazwsx@123");

			SslContextFactory sslContextFactory = new SslContextFactory();
			KeyStore keyStore = keyStoreFactory.newInstance();
			sslContextFactory.setKeyManagerFactoryKeyStore(keyStore);

			KeyStore trustStore = trustStoreFactory.newInstance();
			sslContextFactory.setTrustManagerFactoryKeyStore(trustStore);
			sslContextFactory.setKeyManagerFactoryKeyStorePassword("qazwsx@123");
			context = sslContextFactory.newInstance();
			return context;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static SslFilter createSslFilter(boolean clientMode) {
		SSLContext context = getSslContext();
		SslFilter sslFilter = new SslFilter(context);
		sslFilter.setUseClientMode(clientMode);
		sslFilter.setEnabledProtocols(new String[]{"SSLv3"});
		sslFilter.setEnabledCipherSuites(new String[]{"TLS_RSA_WITH_AES_256_CBC_SHA"});
		logger.info("SSLFilter.EnabledProtocols: " + Arrays.toString(sslFilter.getEnabledProtocols()));
		logger.info("SSLFilter.EnabledCipherSuites2: " + Arrays.toString(sslFilter.getEnabledCipherSuites()));
		return sslFilter;
	}
}

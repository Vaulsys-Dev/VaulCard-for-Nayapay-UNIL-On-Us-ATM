package vaulsys.customer;


import com.thoughtworks.xstream.XStream;
import com.fanap.cms.exception.BusinessException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.NameValuePair;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.*;

public class MyHttpPost {

    static Logger logger = Logger.getLogger(MyHttpPost.class);

    private static XStream xStream;

    static {
        xStream = new XStream();
    }

    public static void postXMLFromFile(String strURL, String strXMLFilename) throws IOException {
        File input = new File(strXMLFilename);
        // Prepare HTTP postXML
        HttpPost post = new HttpPost(strURL);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        // Request content will be retrieved directly
        // from the input stream
        FileEntity entity = new FileEntity(input, "text/xml; charset=UTF-8");
        post.setEntity(entity);
        // Get HTTP client
        HttpClient httpclient = new DefaultHttpClient();
        // Execute request
        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String response = httpclient.execute(post, responseHandler);
            // Display status code
            //logger.debug("Response status code: " + result);
            // Display response
            logger.debug("Response body: ");
            logger.debug(response);
        } finally {
            // Release current connection to the connection pool once you are done
            httpclient.getConnectionManager().shutdown();
        }
    }

    // Get HTTP client
    private static Map<HttpClient, Boolean> httpClients;
    private static Integer timeOut = 500;

    static {
        httpClients = new Hashtable<HttpClient, Boolean>();
        httpClients.put(new DefaultHttpClient(), true);
        httpClients.put(new DefaultHttpClient(), true);
        httpClients.put(new DefaultHttpClient(), true);
        httpClients.put(new DefaultHttpClient(), true);
        httpClients.put(new DefaultHttpClient(), true);
        httpClients.put(new DefaultHttpClient(), true);
        httpClients.put(new DefaultHttpClient(), true);
        httpClients.put(new DefaultHttpClient(), true);
        httpClients.put(new DefaultHttpClient(), true);
        httpClients.put(new DefaultHttpClient(), true);      //10

        for (HttpClient httpClient : httpClients.keySet()) {
            httpClient.getParams().setParameter("http.socket.timeout", timeOut);
            setupForSSLSupport(httpClient);
        }
    }

    private synchronized static HttpClient getHttpClient() throws BusinessException {
        try {
            HttpClient h = null;
            for (HttpClient httpClient : httpClients.keySet()) {
                if (httpClients.get(httpClient)) {
                    h = httpClient;
                }
            }
            if (h != null) {
                httpClients.remove(h);
                httpClients.put(h, false);
            } else {
                h = new DefaultHttpClient();
                h.getParams().setParameter("http.socket.timeout", timeOut);
                setupForSSLSupport(h);
            }
            return h;
        } catch (Throwable e) {
            logger.error(e, e);
            throw new BusinessException(e);
        }
    }

    private synchronized static void freeHttpClient(HttpClient httpClient) throws BusinessException {
        try {
            if (httpClients.containsKey(httpClient)) {
                httpClients.remove(httpClient);
                httpClients.put(httpClient, true);
            } else {
                httpClient.getConnectionManager().shutdown();
            }
        } catch (Throwable e) {
            logger.error(e, e);
            throw new BusinessException(e);
        }
    }

/*    public static void setTimeOut(Integer timeOut) {
        timeOut = timeOut * 1000;
        if (httpClients != null) {
            for (HttpClient httpClient : httpClients.keySet()) {
                httpClient.getParams().setParameter("http.socket.timeout", coreTimeOut);
            }
        }
        logger.debug("coreTimeOut changed to " + coreTimeOut);
    }
*/
    public static String postXML(String strURL, String content) throws IOException, BusinessException {
        // Prepare HTTP postXML
        HttpPost post = new HttpPost(strURL);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("usecaseXML", content));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        post.setEntity(entity);

        // Request content will be retrieved directly from the input stream
        //RequestEntity entity = new StringRequestEntity("", "text/html", "UTF-8");
        //post.setRequestEntity(entity);
        //post.addRequestHeader("usecaseXML", content);

        // Execute request
        HttpClient httpClient = getHttpClient();
        try {
            Date sendDate = new Date();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            logger.debug("......HTTP Req sent.");
            String response = httpClient.execute(post, responseHandler);
            logger.debug("......HTTP Res received. SpendingTime: Thread " + Thread.currentThread().getId() + ": " + ((new Date()).getTime() - sendDate.getTime()) + " (ms)");

            return response;
        } finally {
            // Release current connection to the connection pool once you are done
            freeHttpClient(httpClient);
        }
    }

    public static String postHttpRequest(String strURL, List<String> paramNames, List<String> paramValues) throws IOException, BusinessException {
//        logger.debug("START postHttpRequest ====================");
        // Prepare HTTP postXML
        HttpPost post = new HttpPost(strURL);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

        String paramName;
        String paramVal;
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        for (int i = 0; i < paramNames.size(); i++) {
            paramName = paramNames.get(i);
            paramVal = paramValues.get(i);
//          logger.debug(paramName + " = " + paramVal);
            formparams.add(new BasicNameValuePair(paramName, paramVal));
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        post.setEntity(entity);

        // Execute request
        HttpClient httpClient = getHttpClient();
        try {
            Date sendDate = new Date();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            logger.debug("......HTTP Req sent.");
            String response = httpClient.execute(post, responseHandler);
            logger.debug("......HTTP Res received. SpendingTime: Thread " + Thread.currentThread().getId() + ": " + (new Date().getTime() - sendDate.getTime()) + " (ms)");

            return response;
        } finally {
            // Release current connection to the connection pool once you are done
            freeHttpClient(httpClient);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// Private
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void setupForSSLSupport(HttpClient httpClient) {
        try {
            X509TrustManager trustManager = new MyX509TrustManager();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] {trustManager}, null);

            SSLSocketFactory sf = new SSLSocketFactory(sslContext);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            ClientConnectionManager ccm = httpClient.getConnectionManager();
            SchemeRegistry schemeRegistry = ccm.getSchemeRegistry();

            schemeRegistry.register(new Scheme("https", sf, 443));
        } catch (NoSuchAlgorithmException e) {
            logger.error(e, e);
        } catch (KeyManagementException e) {
            logger.error(e, e);
        }
    }

    private static class MyX509TrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}

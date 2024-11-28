package vaulsys.webservice.keyvault.entity;

import com.jcraft.jsch.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;
import org.apache.log4j.Logger;
import vaulsys.security.keystore.KeyType;
import vaulsys.util.WebServiceUtil;

import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mati on 28/08/2019.
 */

public class KeyVaultServer {

    private Logger logger = Logger.getLogger(this.getClass());

    public Map<String, String> getKeyfromVault() {

        try {

            Client client = Client.create();
            client.setConnectTimeout(30000);
            client.setReadTimeout(30000);
            WebResource webResource = null;
            KeyVaultWsEntity req, resp;
            String line = "", encKey = "", roleId = "", secretId = "", clientToken = "", token = "", decKey = "";
            JSch jSch;
            Session session = null;
            Map<String, String> keyMap = new HashMap<String, String>();
            File file;
            BufferedReader bufferedReader;

            try {

                try {
                    logger.info("Accessing Key Vault Server ...");

                    jSch = new JSch();
                    // Asim Shahzad, Date : 26th June 2020,, Desc : Replaced IP address with URL
                    //session = jSch.getSession("vaulsys", "192.168.26.49", 10984);
                    session = jSch.getSession("vaulsys", "ftp.nayapay.com", 10984);
                    //==========================================================================
                    session.setPassword("V@ulm!ddJalis12!");
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.connect();
                    logger.info("Session Connected");

                    Channel channel = session.openChannel("sftp");
                    channel.connect();
                    logger.info("SFTP Channel Connected");

                    ChannelSftp sftpChannel = (ChannelSftp) channel;
                    sftpChannel.get("/vaulsys/keyfile.txt", "keyfile.txt");
                    logger.info("Key File Read Successfully from SFTP");
                    sftpChannel.get("/vaulsys/switch_middleware.cert", "switch_middleware.cert");
                    logger.info("Public Certificate File Read Successfully from SFTP");
                    sftpChannel.get("/vaulsys/middleware_switch.key", "middleware_switch.key");
                    logger.info("Private Certificate Key File Read Successfully from SFTP");
                    sftpChannel.exit();
                    session.disconnect();

                } catch (JSchException e) {
                    logger.error(WebServiceUtil.getStrException(e));
                    return null;
                } catch (SftpException e) {
                    logger.error(WebServiceUtil.getStrException(e));
                    return null;
                }

                //------------------------------------------
                //routine to fetch pan encrypted key <start>
                //------------------------------------------
                logger.info("//------------------------------------------");
                logger.info("//routine to fetch pan encrypted key <start>");
                logger.info("//------------------------------------------");

                try {

                    file = new File("keyfile.txt");
                    bufferedReader = new BufferedReader(new FileReader(file));
                    logger.debug("File Read Successfully!!!");
                    int index;

                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.contains("ENCRYPTED_KEY: vault:v1:")) {
                            //index = line.lastIndexOf("ENCRYPTED_KEY: vault:v1:");
                            index = 15;
                            encKey = line.substring(index, line.length()).trim();
                            logger.debug("Encrypted Key [" + encKey + "]");
                        }

                        if (line.contains("ROLE_ID: ")) {
                            //index = line.indexOf("ROLE_ID: ");
                            index = 9;
                            roleId = line.substring(index, line.length());
                            logger.debug("Role ID [" + roleId + "]");
                        }

                        if (line.contains("SECRET_ID: ")) {
                            //index = line.indexOf("SECRET_ID: ");
                            index = 11;
                            secretId = line.substring(index, line.length());
                            logger.debug("Secret ID [" + secretId + "]");
                        }
                    }

                    if (file.delete()) {
                        logger.debug("Local Key File copy deleted successfully!!!");
                    }

                    file = new File("switch_middleware.cert");
                    bufferedReader = new BufferedReader(new FileReader(file));
                    logger.debug("Certificate File Read Successfully!!!");
                    String pubKey = "";
                    line = "";

                    while ((line = bufferedReader.readLine()) != null) {
                        pubKey += line;
                    }
                    logger.debug("Certificate [" + pubKey + "]");
                    pubKey = Base64.base64Decode(pubKey).toString();
                    logger.debug("Decoded Certificate [" + pubKey + "]");
                    pubKey = pubKey.substring(26, pubKey.indexOf("-----END PUBLIC KEY-----"));
                    logger.debug("Trimmed Certificate [" + pubKey + "]");
                    pubKey = pubKey.replaceAll("\r", "").replaceAll("\n", "");
                    logger.debug("Final Certificate [" + pubKey + "]");

                    if (file.delete()) {
                        logger.debug("Local Certificate file copy deleted successfully!!!");
                    }

                    keyMap.put(KeyType.TYPE_RSA_PUBLIC, pubKey);
                    logger.info("Public Key Certificate loaded Successfully!!!");

                } catch (Exception e) {
                    logger.error(WebServiceUtil.getStrException(e));
                }

                req = new KeyVaultWsEntity();
                req.setRole_id(roleId);
                req.setSecret_id(secretId);
                logger.debug("Calling URL [https://vault.nayapay.com:8200/v1/auth/approle/login]");
                webResource = client.resource("https://vault.nayapay.com:8200/v1/auth/approle/login");
                resp = webResource.type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(KeyVaultWsEntity.class, req);
                logger.debug("Response Received: Client Token [" + resp.getAuth().getClient_token() + "], Replying to Acquirer...");
                clientToken = resp.getAuth().getClient_token();

                logger.debug("Calling URL [https://vault.nayapay.com:8200/v1/switchapp/data/accesstoken]");
                webResource = client.resource("https://vault.nayapay.com:8200/v1/switchapp/data/accesstoken");
                resp = webResource
                        .header("X-Vault-Token", clientToken)
                        .type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .get(KeyVaultWsEntity.class);
                logger.debug("Response Received: Token [" + resp.getData().getData().getToken() + "], Replying to Acquirer...");
                token = resp.getData().getData().getToken();

                req = new KeyVaultWsEntity();
                req.setCiphertext(encKey);
                logger.debug("Calling URL [https://vault.nayapay.com:8200/v1/dekencrypt/decrypt/switchkey]");
                webResource = client.resource("https://vault.nayapay.com:8200/v1/dekencrypt/decrypt/switchkey");
                resp = webResource
                        .header("X-Vault-Token", token)
                        .type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(KeyVaultWsEntity.class, req);
                logger.debug("Response Received: Key [" + resp.getData().getPlaintext() + "], Replying to Acquirer...");
                decKey = resp.getData().getPlaintext();

                keyMap.put(KeyType.TYPE_AES, decKey);
                logger.info("PAN Encryption Key loaded Successfully!!!");
				
                logger.info("//----------------------------------------");
                logger.info("//routine to fetch pan encrypted key <end>");
                logger.info("//----------------------------------------");
                //----------------------------------------
                //routine to fetch pan encrypted key <end>
                //-----------------------------------------

                //------------------------------------------
                //routine to fetch pan encrypted key <start>
                //------------------------------------------
                logger.info("//------------------------------------------");
                logger.info("//routine to fetch pan decrypted key <start>");
                logger.info("//------------------------------------------");

                try {
                file = new File("middleware_switch.key");
                bufferedReader = new BufferedReader(new FileReader(file));
                logger.debug("File Read Successfully!!!");
                int index;

                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("ENCRYPTED_CERT: vault:v1:")) {
                        //index = line.lastIndexOf("ENCRYPTED_KEY: vault:v1:");
                        index = 16;
                        encKey = line.substring(index, line.length()).trim();
                        logger.debug("Encrypted Cert [" + encKey + "]");
                    }

                    if (line.contains("ROLE_ID: ")) {
                        //index = line.indexOf("ROLE_ID: ");
                        index = 9;
                        roleId = line.substring(index, line.length());
                        logger.debug("Role ID [" + roleId + "]");
                    }

                    if (line.contains("SECRET_ID: ")) {
                        //index = line.indexOf("SECRET_ID: ");
                        index = 11;
                        secretId = line.substring(index, line.length());
                        logger.debug("Secret ID [" + secretId + "]");
                        }
                    }

                    if (file.delete()) {
                        System.out.println("Private Certificate key file copy deleted successfully!!!");
                    }

                } catch (Exception e) {
                    logger.error(WebServiceUtil.getStrException(e));
                }

                req.setRole_id(roleId);
                req.setSecret_id(secretId);
                logger.debug("Calling URL [https://vault.nayapay.com:8200/v1/auth/approle/login]");
                webResource = client.resource("https://vault.nayapay.com:8200/v1/auth/approle/login");
                resp = webResource.type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(KeyVaultWsEntity.class, req);
                logger.debug("Response Received: Client Token [" + resp.getAuth().getClient_token() + "], Replying to Acquirer...");
                clientToken = resp.getAuth().getClient_token();

                logger.debug("Calling URL [https://vault.nayapay.com:8200/v1/chdtransittoken/middlewaretoswitch]");
                webResource = client.resource("https://vault.nayapay.com:8200/v1/chdtransittoken/middlewaretoswitch");
                resp = webResource
                        .header("X-Vault-Token", clientToken)
                        .type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .get(KeyVaultWsEntity.class);
                logger.debug("Response Received: Token [" + resp.getData().getToken() + "], Replying to Acquirer...");
                token = resp.getData().getToken();

                req = new KeyVaultWsEntity();
                req.setCiphertext(encKey);
                logger.debug("Calling URL [https://vault.nayapay.com:8200/v1/chdtransmit/decrypt/certkey]");
                webResource = client.resource("https://vault.nayapay.com:8200/v1/chdtransmit/decrypt/certkey");
                resp = webResource
                        .header("X-Vault-Token", token)
                        .type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(KeyVaultWsEntity.class, req);
                logger.debug("Response Received: Key [" + resp.getData().getPlaintext() + "], Replying to Acquirer...");
                decKey = resp.getData().getPlaintext();
                decKey = Base64.base64Decode(decKey).toString();
                logger.debug("Decoded Certificate [" + decKey + "]");
                decKey = decKey.substring(31, decKey.indexOf("-----END RSA PRIVATE KEY-----"));
                logger.debug("Trimmed Certificate [" + decKey + "]");
                decKey = decKey.replaceAll("\r", "").replaceAll("\n", "");
                logger.debug("Final Certificate [" + decKey + "]");

                keyMap.put(KeyType.TYPE_RSA_PRIVATE, decKey);
                logger.info("PAN Decryption Key loaded Successfully!!!");
				
				logger.info("//----------------------------------------");
                logger.info("//routine to fetch pan decrypted key <end>");
                logger.info("//----------------------------------------");
                //----------------------------------------
                //routine to fetch pan encrypted key <end>
                //----------------------------------------

            } catch (Exception e) {
                logger.error(WebServiceUtil.getStrException(e));
            } finally {
                logger.info("Destroying client for API call, deleting request...");
                if (client != null) {
                    client.destroy();
                }

                return keyMap;
            }
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
        }

        return null;
    }
}

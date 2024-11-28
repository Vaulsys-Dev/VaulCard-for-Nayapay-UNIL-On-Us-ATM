package vaulsys.security.hsm.eracom;

/*
 * Implemented functions of ESM
 * 1)generateMAC
 * 		MAC_GEN_UPDATE				EE0700
 * 		MAC_VER_FINAL				EE0701
 * 		MAC_VER_FINAL				EE0702
 * 2)HSM_Status						01
 * 3)CLR_PIN_ENCRYPT				EE0600
 * 4)translatePIN					EE0602
 * 5)generateTerminalSessionKey		EE0400
 * 6)encrypt						EE0800
 * 7)decrypt						EE0801
 * 8)generateInitialSessionKeys		EE0402
 */

import vaulsys.config.ConfigurationManager;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.OutputChannel;
import vaulsys.security.base.HSMDriver;
import vaulsys.security.exception.SMException;
import vaulsys.security.hsm.DefaultHSMHandler;
import vaulsys.security.hsm.eracom.KeySpec.KeySpec00;
import vaulsys.security.hsm.eracom.KeySpec.KeySpec01;
import vaulsys.security.hsm.eracom.KeySpec.KeySpec02;
import vaulsys.security.hsm.eracom.KeySpec.KeySpec03;
import vaulsys.security.hsm.eracom.KeySpec.KeySpec10;
import vaulsys.security.hsm.eracom.KeySpec.KeySpec11;
import vaulsys.security.hsm.eracom.KeySpec.KeySpec13;
import vaulsys.security.hsm.eracom.KeySpec.KeySpec14;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.security.hsm.eracom.MAC.MAC;
import vaulsys.security.hsm.eracom.base.CryptoMode;
import vaulsys.security.hsm.eracom.base.ErrorCode;
import vaulsys.security.hsm.eracom.base.FunctionCode;
import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.security.hsm.eracom.base.Message;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.ssm.base.BaseSMAdapter;
import vaulsys.security.ssm.fanapSSM.VaulsysSSMDriver;
import vaulsys.util.MyInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.IoFilter;

import sun.management.ConnectorAddressLink;

public class ESMDriver extends BaseSMAdapter implements HSMDriver {

    private static ESMDriver eSMDriver;

    private static int MAX_NUMBER_OF_CONNECTION = 1;

    private static String NAME;                                                                            // = "Ercom HSM";
    private static String IP;
    private static int Port;
    private static boolean Fixed_Length = false;
    private static boolean Meta_Function = false;

    private final Semaphore available = new Semaphore(MAX_NUMBER_OF_CONNECTION, true);

    List<DefaultHSMHandler> hsmHandlers;

    Map<ConnectorAddressLink, Boolean> hsmConnections;
    OutputChannel hsmChannel;

    private Logger logger = Logger.getLogger(ESMDriver.class);

    public static void configDriver(/* String configFile */) {
        Configuration config = ConfigurationManager.getInstance().getConfiguration("ESM_Driver");
        ESMDriver.NAME = config.getString("Driver/Name");
        ESMDriver.IP = config.getString("Driver/IP");
        ESMDriver.Port = config.getInt("Driver/Port");
        ESMDriver.MAX_NUMBER_OF_CONNECTION = config.getInt("Driver/NumberOfConnection");
        // TODO do some checking here!
        ESMDriver.Fixed_Length = config.getBoolean("Driver/Extera_info/FixedLength");
        ESMDriver.Meta_Function = config.getBoolean("Driver/Extera_info/MetaFunction");
    }

    private void initESMConnection() {
        try {
            hsmChannel = new OutputChannel(ESMDriver.IP, ESMDriver.Port, "FanapHSMChannel", "", "", "", ESMIoFilter.class.getName(), CommunicationMethod.SAME_SOCKET, "2", -1, false, false, null, null, false, 0, false, 0, null, null,0);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // BusSvc.getInstance().getBusElement("Bus.Configuration.ChannelList").addBusElement(ssmChannel.toXML());

        /*
           * hsmHandlers = new ArrayList<DefaultHSMHandler>(); for (int i = 0; i < VaulsysSSMDriver.MAX_NUMBER_OF_CONNECTION; i++) { DefaultHSMHandler handler = new DefaultHSMHandler();
           * hsmHandlers.add(handler); NetworkConnection nc = new NetworkConnection(hsmChannel, null, handler); nc.connect(); handler.waitForSession(); }
           */

        hsmConnections = new HashMap<ConnectorAddressLink, Boolean>();
        List<IoFilter> filters = new ArrayList<IoFilter>();
        filters.add(new ESMIoFilter());

        hsmHandlers = new ArrayList<DefaultHSMHandler>();
        for (int i = 0; i < VaulsysSSMDriver.MAX_NUMBER_OF_CONNECTION; i++) {
            DefaultHSMHandler handler = new DefaultHSMHandler();
            hsmHandlers.add(handler);
            // NetworkConnection nc = new NetworkConnection(hsmChannel, null, handler);
//            hsmConnections.put(hsmConnection, true);
//            hsmConnection.connect();
            // handler.waitForSession();
        }
    }

    private void getConnection(/*Connector connector*/) {
//        hsmConnections.put(connector, false);
    }

    private void releaseConnection(/*Connector connector*/) {
//        hsmConnections.put(connector, true);
    }

    private void getFreeConnection() {

        synchronized (hsmConnections) {
            /*for (Connector connector : hsmConnections.keySet()) {
                if (hsmConnections.get(connector)) {
                    if (!connector.session.isConnected()) {
                        connector.reconnect();
                    }
                    getConnection(connector);
                    return connector;
                }
            }*/
        }

//        return null;
    }

    private void repairConnection(/*Connector connector*/) {/*
        this.hsmConnections.remove(connector);
//        connector.reconnect();
        this.hsmConnections.put(connector, true);
    */}

    private ESMDriver() {
        initESMConnection();
    }

    public static ESMDriver getInstance() {
        if (eSMDriver == null) {
            ESMDriver.configDriver();

            eSMDriver = new ESMDriver();
        }
        return eSMDriver;
    }

    @Override
    public String getName() {
        return ESMDriver.NAME;
    }

    @Override
    public SecureDESKey generateKey(short keyLength, String keyType) throws SMException {
        try {
            available.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

/*
        if (KeyType.TYPE_TMK.equals(keyType)) {
            byte[] bytes = generateTerminalMasterKey(new PrintData[0], new PrintData[0]);
            return new SecureDESKey();
        } else if (KeyType.TYPE_.equals(keyType)) {

        }
*/
        available.release();

        return null;
    }

    /*
      * MAC Management Functions 1)generateMAC MAC_GEN_UPDATE EE0700 MAC_VER_FINAL EE0701 MAC_VER_FINAL EE0702
      */

    @Override
    public boolean verifyMAC(int keyIndex, byte[] keyData, byte[] data, byte[] mac, int algorithm, byte[] IV, int padding, byte[] masterKey) {
        try {/*
            available.acquire();
            // DefaultHSMHandler handler = getFreeConnection();
//            Connector connector = getFreeConnection();
            boolean result = false;

            // if (handler != null) {
            if (connector != null) {

                // TODO format of key must be clear!!
                // if KeyData = null then the key must be hsm-stored key (00, 01,02, 03)
                byte format = (byte) 0x00;

                if (keyData == null) {
                    // hsm-stored key
                    if (0 <= keyIndex && keyIndex < 100) {
                        format = (byte) 0x00;
                    } else if (100 <= keyIndex && keyIndex < 1000)
                        format = (byte) 0x03;
                } else {
                    // TODO host-stored key
                    if (keyData.length == 8) {
                        format = (byte) 0x10;
                        // System.out.println("Key: Format " + 10);
                    } else if (keyData.length == 8) {
                        format = (byte) 0x13;
                        // System.out.println("Key: Format " + 13);
                    }
                }

                KeySpecifier keySpec = getKey(format, keyIndex, keyData);

                byte[] d = new byte[8];

                int i;

                for (i = 0; i < (data.length / 8); i++) {
                    System.arraycopy(data, i * 8, d, 0, 8);

                    IV = MAC_GEN_UPDATE(keySpec, d, algorithm, IV, (DefaultHSMHandler) connector.getHandler() handler , i);

                    // System.out.println("IV: " + HSMUtil.byteToString(IV));
                    if (IV == null) {
                        logger.warn("Error: IV = null.");
                        releaseConnection(connector);
                        releaseConnection(connector);
                        available.release();
                        return false;
                    }
                }

                d = new byte[data.length - i * 8];
                System.arraycopy(data, i * 8, d, 0, d.length);

                byte[] newMac = MAC_GEN_FINAL(keySpec, d, algorithm, IV, mac.length, padding, (DefaultHSMHandler) connector.getHandler(), i);

                for (int j = 0; j < mac.length; j++)
                    if (newMac[j] != mac[j]) {
                        releaseConnection(connector);
                        releaseConnection(connector);
                        available.release();
                        return false;
                    }

                // result = MAC_VER_FINAL(keySpec, data, mac, algorithm, IV, padding, handler, i);
                releaseConnection(connector);
                releaseConnection(connector);
                available.release();
                return true;
            }
       } catch (InterruptedException e) {
            logger.error("Error: availabe semaphor cannot be acquired "+e, e);
      */   } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        available.release();
        return false;
    }

    public byte[] generateMAC(int keyIndex, byte[] keyData, byte[] data, int algorithm, byte[] IV, int macLength, int padding, byte[] masterKey) {
//        Connector connector = null;
      /*  try {
            available.acquire();
            connector = getFreeConnection();
            DefaultHSMHandler handler = (DefaultHSMHandler) connector.getHandler();

            byte[] mac;

            if (handler != null) {

                byte format = getFormat(keyIndex, keyData, algorithm);

                KeySpecifier keySpec = getKey(format, keyIndex, keyData);

                byte[] d = new byte[8];

                int i;

                for (i = 0; i < (data.length / 8); i++) {
                    System.arraycopy(data, i * 8, d, 0, 8);

                    IV = MAC_GEN_UPDATE(keySpec, d, algorithm, IV, handler, i);

                    if (IV == null) {
                        logger.warn("Error: IV = null.");
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }
                }

                d = new byte[data.length - i * 8];
                System.arraycopy(data, i * 8, d, 0, d.length);

                mac = MAC_GEN_FINAL(keySpec, d, algorithm, IV, macLength, padding, handler, i);
                releaseConnection(connector);
                available.release();
                return mac;

            }
        } catch (InterruptedException e) {
            logger.error("Error: availabe semaphor cannot be acquired "+ e, e);
        } catch (SessionClosedException e) {
            available.release();
            repairConnection(connector);
        }
*/
        return null;
    }

    private byte[] MAC_GEN_UPDATE(KeySpecifier keySpec, byte[] data, int algorithm, byte[] IV, DefaultHSMHandler handler, int i) throws SessionClosedException {

        IV = MAC.MAC_Gen_Update(keySpec, data, algorithm, IV);

        IV = Message.putFuncCode(IV, FunctionCode.MAC_GEN_UPDATE, (byte) 0x00, ESMDriver.Fixed_Length);

        // byte[] secondMessage = Message.putMetaFunctionHeader(IV, (byte) 0x01, (byte) 0x01, Message.createMessageID(i + 2, 4));

        if (ESMDriver.Meta_Function)
            IV = Message.putMetaFunctionHeader(IV, Message.MTI_01, Message.MTV, Message.createMessageID(i, 4));

        IV = Message.putCommonHeader(IV, Message.HVN, Message.createMessageID(i, 2));
        // secondMessage = Message.putCommonHeader(secondMessage, (byte) 0x01, Message.createMessageID(1, 2));

        // byte[] buffer = new byte[IV.length + secondMessage.length];
        // System.arraycopy(IV, 0, buffer, 0, IV.length);
        // System.arraycopy(secondMessage, 0, buffer, IV.length, secondMessage.length);

        // IV = buffer;

        // IV = handler.sendMessageReceiveResponse(IV);
        /*
           * byte[] request = new byte[] { 0x01, 0x01, 0x00, 0x00, 0x00, 0x2C, (byte) 0xE3, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x23, (byte) 0xEE, 0x08, 0x00, (byte) 0xFF, (byte) 0xFF, (byte)
           * 0xFF, 0x00, 0x09, 0x10, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
           *///
        IV = handler.sendMessageReceiveResponse(IV);

        if (IV == null || IV[0] == (byte) 0xFF) {
            // HSM closed session without sending any response!!
            throw new SessionClosedException();

        } else {

            IV = Message.takeCommonHeader(IV, Message.HVN, Message.createMessageID(i, 2));
            if (IV.length <= 2) {
                logger.warn("An Error was occured: " + ErrorCode.getDescription(IV[1]));
                return null;
            }

            if (ESMDriver.Meta_Function) {
                IV = Message.takeMetaFunctionHeader(IV, Message.MTI_01, Message.MTV, Message.createMessageID(i, 4));
                if (IV.length <= 2) {
                    logger.warn("An Error was occured: " + ErrorCode.getDescription(IV[1]));
                    return null;
                }
            }

            IV = Message.takeFuncCode(IV, FunctionCode.MAC_GEN_UPDATE, ESMDriver.Fixed_Length);
            if (IV.length <= 2) {
                logger.warn("An Error was occured: " + ErrorCode.getDescription(IV[1]));
                return null;
            }

            return IV;
        }

    }

    private byte[] MAC_GEN_FINAL(KeySpecifier keySpec, byte[] data, int algorithm, byte[] IV, int macLength, int padding, DefaultHSMHandler handler, int i) throws SessionClosedException {

        IV = MAC.MAC_Gen_Final(keySpec, data, algorithm, IV, macLength, padding);
        IV = Message.putFuncCode(IV, FunctionCode.MAC_GEN_FINAL, (byte) 0x00, ESMDriver.Fixed_Length);
        if (ESMDriver.Meta_Function)
            IV = Message.putMetaFunctionHeader(IV, Message.MTI_01, Message.MTV, Message.createMessageID(i, 4));
        IV = Message.putCommonHeader(IV, Message.HVN, Message.createMessageID(i, 2));

        IV = handler.sendMessageReceiveResponse(IV);

        if (IV == null || IV[0] == (byte) 0xFF) {
            // HSM closed session without sending any response!!
            throw new SessionClosedException();
        } else {

            IV = Message.takeCommonHeader(IV, Message.HVN, Message.createMessageID(i, 2));
            if (IV.length <= 2) {
                logger.warn("An Error was occured: " + ErrorCode.getDescription(IV[1]));
                return null;
            }

            if (ESMDriver.Meta_Function) {
                IV = Message.takeMetaFunctionHeader(IV, Message.MTI_01, Message.MTV, Message.createMessageID(i, 4));
                if (IV.length <= 2) {
                    logger.warn("An Error was occured: " + ErrorCode.getDescription(IV[1]));
                    return null;
                }
            }

            IV = Message.takeFuncCode(IV, FunctionCode.MAC_GEN_FINAL, ESMDriver.Fixed_Length);
            if (IV.length <= 2) {
                logger.warn("An Error was occured: " + ErrorCode.getDescription(IV[1]));
                return null;
            }

        }
        if (HSMUtil.getLengthOfVarField(IV, new MyInteger(0)) != macLength) {
            logger.warn("An Error was occured: Invalid MAC length");
            return null;
        }

        byte[] mac = new byte[macLength];
        System.arraycopy(IV, 1, mac, 0, macLength);

        return mac;
    }

    private boolean MAC_VER_FINAL(KeySpecifier keySpec, byte[] data, byte[] mac, int algorithm, byte[] IV, int padding, DefaultHSMHandler handler, int i) throws SessionClosedException {

        IV = MAC.MAC_Ver_Final(keySpec, data, algorithm, IV, mac, padding);
        IV = Message.putFuncCode(IV, FunctionCode.MAC_VER_FINAL, (byte) 0x00, true);
        if (ESMDriver.Meta_Function)
            IV = Message.putMetaFunctionHeader(IV, Message.MTI_01, Message.MTV, Message.createMessageID(i, 4));
        IV = Message.putCommonHeader(IV, Message.HVN, Message.createMessageID(i, 2));

        IV = handler.sendMessageReceiveResponse(IV);

        if (IV == null || IV[0] == (byte) 0xFF) {
            // HSM closed session without sending any response!!
            throw new SessionClosedException();
        } else {

            IV = Message.takeCommonHeader(IV, Message.HVN, Message.createMessageID(i, 2));
            if (IV.length <= 2) {
                logger.warn("An Error was occured: " + ErrorCode.getDescription(IV[1]));
                return false;
            }

            if (ESMDriver.Meta_Function) {
                IV = Message.takeMetaFunctionHeader(IV, Message.MTI_01, Message.MTV, Message.createMessageID(i, 4));
                if (IV.length <= 2) {
                    logger.warn("An Error was occured: " + ErrorCode.getDescription(IV[1]));
                    return false;
                }
            }

            IV = Message.takeFuncCode(IV, FunctionCode.MAC_VER_FINAL, true);
            if (IV.length <= 2) {
                logger.warn("An Error was occured: " + ErrorCode.getDescription(IV[1]));
                return false;
            }

        }
        return true;
    }

    /*
      * HSM Status Functions 1)HSM_Status 01
      */

    public String HSM_Status() {/*

        try {
            available.acquire();
            Connector connector = getFreeConnection();

            byte[] request = FunctionCode.HSM_STATUS;
            // request = Message.putMetaFunctionHeader(request, (byte) 0x01, (byte) 0x01, Message.createMessageID(1, 4));
            request = Message.putCommonHeader(request, Message.HVN, new byte[]{0, 0});

            // request = handler.sendMessageReceiveResponse(request);
            request = ((DefaultHSMHandler) connector.getHandler()).sendMessageReceiveResponse(request);

            request = Message.takeCommonHeader(request, Message.HVN, new byte[]{0, 0});

            if (request == null)
                repairConnection(connector);
            else {

                if (request.length <= 2) {
                    logger.warn("An Error was occured: " + ErrorCode.getDescription(request[1]));
                    releaseConnection(connector);
                    available.release();
                    return null;
                }

                
                     * request = Message.takeMetaFunctionHeader(request, (byte) 0x01, (byte) 0x01, Message.createMessageID(1, 4)); if (request.length <= 2){ logger.error("An Error was occured" +
                     * ErrorCode.getDescription(request[1])); return null; }
                     

                HSMStatusResp resp = new HSMStatusResp(request, 1);

                if (resp != null) {
                    releaseConnection(connector);
                    available.release();
                    return resp.toString();
                }
            }
        } catch (InterruptedException e) {
            logger.error("Error", e);
        }

        available.release();
    */
    	return null;
    	
    }

    /*
      * PIN Management Functions 1)CLR_PIN_ENCRYPT ee0600 2)PIN-TRAN-2 EE0602
      */
    public byte[] CLR_PIN_ENCRYPT(String PIN, String accountNumber, int keyIndex) {/*
        try {
            available.acquire();
            Connector connector = getFreeConnection();
            DefaultHSMHandler handler = (DefaultHSMHandler) connector.getHandler();
            if (handler != null) {
                byte[] request = PINManagement.Clr_PIN_Encrypt(PIN, accountNumber, keyIndex);
                request = Message.putFuncCode(request, FunctionCode.CLR_PIN_ENCRYPT, (byte) 0x00, true);
                if (ESMDriver.Meta_Function)
                    request = Message.putMetaFunctionHeader(request, Message.MTI_01, Message.MTV, Message.createMessageID(1, 4));
                request = Message.putCommonHeader(request, Message.HVN, Message.createMessageID(1, 2));

                request = handler.sendMessageReceiveResponse(request);

                if (request == null)
                    repairConnection(connector);
                else {

                    request = Message.takeCommonHeader(request, Message.HVN, Message.createMessageID(1, 2));
                    if (request.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(request[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    if (ESMDriver.Meta_Function) {
                        request = Message.takeMetaFunctionHeader(request, Message.MTI_01, Message.MTV, Message.createMessageID(1, 4));
                        if (request.length <= 2) {
                            logger.warn("An Error was occured: " + ErrorCode.getDescription(request[1]));
                            releaseConnection(connector);
                            available.release();
                            return null;
                        }
                    }
                    request = Message.takeFuncCode(request, FunctionCode.CLR_PIN_ENCRYPT, true);
                    if (request.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(request[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    if (request.length < 8) {
                        logger.warn("Invalid Encrypted PIN Length: " + request.length);
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }
                    releaseConnection(connector);
                    available.release();
                    return request;
                }
            }
        } catch (InterruptedException e) {
            logger.error("Error ", e);
        }
        available.release();
    */
    	
    	return null;
    	}

    public byte[] translatePIN(byte[] inputPinBlock, int inputIndex, byte[] inputKey, byte PFi, String AccountNumberBlock, byte PFo, int outputIndex, byte[] outputKey, byte[] masterKey) {
    	/*
        try {
            available.acquire();

            Connector connector = getFreeConnection();
            DefaultHSMHandler handler = (DefaultHSMHandler) connector.getHandler();
            if (handler != null) {

                byte format = (byte) 0x00;
                if (inputKey == null) {
                    // hsm-stored key
                    if (0 <= inputIndex && inputIndex < 100) {
                        format = (byte) 0x00;
                    } else if (100 <= inputIndex && inputIndex < 1000)
                        format = (byte) 0x03;
                } else {
                    if (inputKey.length == 8) {
                        format = (byte) 0x10;
                    } else if (inputKey.length == 16) {
                        
                               * Note format-11 ECB format-13 CBC
                               
                        format = (byte) 0x11;
                    }
                }

                KeySpecifier inputKeySpec = getKey(format, inputIndex, inputKey);

                format = (byte) 0x00;
                if (outputKey == null) {
                    // hsm-stored key
                    if (0 <= outputIndex && outputIndex < 100) {
                        format = (byte) 0x00;
                    } else if (100 <= outputIndex && outputIndex < 1000)
                        format = (byte) 0x03;
                } else {
                    if (outputKey.length == 8) {
                        format = (byte) 0x10;
                    } else if (outputKey.length == 16) {
                        
                               * Note format-11 ECB format-13 CBC
                               
                        format = (byte) 0x11;
                    }
                }

                KeySpecifier outputKeySpec = getKey(format, outputIndex, outputKey);

                byte[] request = PINManagement.Translate_PIN(inputPinBlock, inputKeySpec, PFi, AccountNumberBlock, PFo, outputKeySpec);

                request = Message.putFuncCode(request, FunctionCode.PIN_TRAN_2, (byte) 0x00, ESMDriver.Fixed_Length);
                if (ESMDriver.Meta_Function)
                    request = Message.putMetaFunctionHeader(request, Message.MTI_01, Message.MTV, Message.createMessageID(0, 4));
                request = Message.putCommonHeader(request, Message.HVN, Message.createMessageID(0, 2));

                byte[] response = handler.sendMessageReceiveResponse(request);
                if (response == null || response[0] == (byte) 0xFF) {
                    // HSM closed session without sending any response!!
                    repairConnection(connector);
                } else {
                    response = Message.takeCommonHeader(response, Message.HVN, Message.createMessageID(0, 2));
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    if (ESMDriver.Meta_Function) {
                        response = Message.takeMetaFunctionHeader(response, Message.MTI_01, Message.MTV, Message.createMessageID(0, 4));
                        if (response.length <= 2) {
                            logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                            releaseConnection(connector);
                            available.release();
                            return null;
                        }
                    }
                    response = Message.takeFuncCode(response, FunctionCode.IT_KEY_GEN, ESMDriver.Fixed_Length);
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    releaseConnection(connector);
                    available.release();
                    return response;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            available.release();
        }*/

        return null;
    }

    /*
      * Transfer Functions
      * 1)Retrieve_Key 21
      * 2)Store_Key	22
      * 3)KEY_IMPORT
      * 4)KEY_EXPORT
      */

    public String Retrieve_Key(KeySpecifier KXTSpec) {/*

        try {
            available.acquire();
            Connector connector = getFreeConnection();
            DefaultHSMHandler handler = (DefaultHSMHandler) connector.getHandler();

            if (handler != null) {

                byte[] data = TransferManagement.Retrieve_Key(KXTSpec);
                byte[] retrieveKey = FunctionCode.Retrieve_Key;
                byte[] funcode = new byte[retrieveKey.length + 1];
                System.arraycopy(retrieveKey, 0, funcode, 0, retrieveKey.length);
                System.arraycopy(new byte[]{0}, 0, funcode, retrieveKey.length, 1);

                data = Message.putFuncCode(data, funcode, (byte) 0x00, true);

                if (ESMDriver.Meta_Function)
                    data = Message.putMetaFunctionHeader(data, Message.MTI_01, Message.MTV, Message.createMessageID(1, 4));
                data = Message.putCommonHeader(data, Message.HVN, Message.createMessageID(1, 2));

                data = handler.sendMessageReceiveResponse(data);

                if (data == null)
                    repairConnection(connector);
                else {

                    data = Message.takeCommonHeader(data, Message.HVN, Message.createMessageID(1, 2));
                    if (data.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(data[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    if (ESMDriver.Meta_Function) {
                        data = Message.takeMetaFunctionHeader(data, Message.MTI_01, Message.MTV, Message.createMessageID(1, 4));
                        if (data.length <= 2) {
                            logger.warn("An Error was occured: " + ErrorCode.getDescription(data[1]));
                            releaseConnection(connector);
                            available.release();
                            return null;
                        }
                    }
                    data = Message.takeFuncCode(data, FunctionCode.Retrieve_Key, false);
                    if (data.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(data[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    HsmRetrieveKey key = new HsmRetrieveKey(data, 0);
                    // TODO the key must be returned
                    releaseConnection(connector);
                    available.release();
                    return key.toString();

                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        available.release();
    */
    	return null;
    	}


    public byte[] KEY_IMPORT(int encryptingKeyIndex, byte[] encryptingKeyData, int mode, int type, byte[] keyData) {/*

        Connector connector = null;

        try {
            available.acquire();
            connector = getFreeConnection();
            DefaultHSMHandler handler = (DefaultHSMHandler) connector.getHandler();

            if (handler != null) {

                byte format = getFormat(encryptingKeyIndex, encryptingKeyData, mode);
                KeySpecifier KIRSpec = getKey(format, encryptingKeyIndex, encryptingKeyData);

                format = getFormat(-1, keyData, mode);
                KeySpecifier keySpec = getKey(format, -1, keyData);

                byte[] requestMessage = TransferManagement.KEY_IMPORT(KIRSpec, type, mode, keySpec);
                requestMessage = Message.putFuncCode(requestMessage, FunctionCode.KEY_IMPORT, (byte) 0x00, ESMDriver.Fixed_Length);
                if (ESMDriver.Meta_Function)
                    requestMessage = Message.putMetaFunctionHeader(requestMessage, Message.MTI_01, Message.MTV, Message.createMessageID(1, 4));

                requestMessage = Message.putCommonHeader(requestMessage, Message.HVN, Message.createMessageID(1, 2));

                byte[] response = handler.sendMessageReceiveResponse(requestMessage);


                if (response == null || response[0] == (byte) 0xFF) {
                    // HSM closed session without sending any response!!
                    throw new SessionClosedException();

                } else {

                    response = Message.takeCommonHeader(response, Message.HVN, Message.createMessageID(1, 2));
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        return null;
                    }

                    if (ESMDriver.Meta_Function) {
                        response = Message.takeMetaFunctionHeader(response, Message.MTI_01, Message.MTV, Message.createMessageID(1, 4));
                        if (response.length <= 2) {
                            logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                            return null;
                        }
                    }

                    response = Message.takeFuncCode(response, FunctionCode.MAC_GEN_UPDATE, ESMDriver.Fixed_Length);
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        return null;
                    }

                    return response;
                }
            }

            available.release();
        } catch (InterruptedException e) {
            logger.error("Error: availabe semaphor cannot be acquired", e);
            e.printStackTrace();
        } catch (SessionClosedException e) {
            logger.error("Error: Connection with ESM was closed by ESM", e);
            repairConnection(connector);
        }

    */
    	return null;
    	}

//    /*
//	 * EFT Terminal Functions 1)Initial Session Key Generation IT_KEY_GEN EE0400
//	 */
//	public byte[] generateTerminalMasterKey(PrintData[] aEnvelope, PrintData[] bEnvelope) {
//
//		Connector connector = null;
//
//		try
//		{
//			available.acquire();
//			connector = getFreeConnection();
//			DefaultHSMHandler handler = (DefaultHSMHandler) connector.getHandler();
//
//			if (handler != null) {
//
//
//				byte[] requestMessage = EFTTerminalMgr.Key_Mailer(aEnvelope, bEnvelope);
//				requestMessage = Message.putFuncCode(requestMessage, FunctionCode.Key_Mailer, (byte)0x00, ESMDriver.Fixed_Length);
//				if (ESMDriver.Meta_Function)
//					requestMessage = Message.putMetaFunctionHeader(requestMessage, Message.MTI_01, Message.MTV, Message.createMessageID(1, 4));
//
//				requestMessage = Message.putCommonHeader(requestMessage, Message.HVN, Message.createMessageID(1, 2));
//
//				byte[] response = handler.sendMessageReceiveResponse(requestMessage);
//
//
//				if (response == null || response[0] == (byte) 0xFF) {
//					// HSM closed session without sending any response!!
//					throw new SessionClosedException();
//
//				} else {
//
//					response = Message.takeCommonHeader(response, Message.HVN, Message.createMessageID(1, 2));
//					if (response.length <= 2) {
//                        logger.error("An Error was occured: " + getKeyMailerErrorDescription(response[1]));
//						return null;
//					}
//
//					if (ESMDriver.Meta_Function) {
//						response = Message.takeMetaFunctionHeader(response, Message.MTI_01, Message.MTV, Message.createMessageID(1, 4));
//						if (response.length <= 2) {
//							logger.error("An Error was occured: " + getKeyMailerErrorDescription(response[1]));
//							return null;
//						}
//					}
//
//					response = Message.takeFuncCode(response, FunctionCode.Key_Mailer, ESMDriver.Fixed_Length);
//					if (response.length <= 2) {
//						logger.error("An Error was occured: " + getKeyMailerErrorDescription(response[1]));
//						return null;
//					}
//
//					return response;
//				}
//			}
//
//			available.release();
//		} catch (InterruptedException e)
//		{
//			logger.error("Error: availabe semaphor cannot be acquired", e);
//			e.printStackTrace();
//		} catch (SessionClosedException e) {
//			logger.error("Error: Connection with ESM was closed by ESM", e);
//			repairConnection(connector);
//		}
//
//		return null;
//	}

    private String getKeyMailerErrorDescription(byte b) {
        switch (b) {
            case 0x02:
                return "Illegal Function Code, Key Mailer facility was not enabled.";
            case 0x04:
                return "Invalid data in message";
            case 0x0B:
                return "Printer is not operable.";
            default:
                return ErrorCode.getDescription(b);
        }
    }

    /*
	 * EFT Terminal Functions 1)Initial Session Key Generation IT_KEY_GEN EE0400
	 */
    public byte[] generateTerminalSessionKey(int keyIndex, byte[] keyData, int mode, int[] keyFlags) {/*
        try {
            available.acquire();

            Connector connector = getFreeConnection();
            DefaultHSMHandler handler = (DefaultHSMHandler) connector.getHandler();

            if (handler != null) {
                byte format = getFormat(keyIndex, keyData, mode);

                KeySpecifier keySpec = getKey(format, keyIndex, keyData);

                int id = 25;
                byte[] request;

                request = EFTTerminalMgr.IT_Key_Gen(keySpec, keyFlags, mode);
                request = Message.putFuncCode(request, FunctionCode.IT_KEY_GEN, (byte) 0x00, false  true );
                if (ESMDriver.Meta_Function)
                    request = Message.putMetaFunctionHeader(request, Message.MTI_01, Message.MTV, Message.createMessageID(id, 4));
                request = Message.putCommonHeader(request, Message.HVN, Message.createMessageID(id, 2));

                byte[] response;
                response = handler.sendMessageReceiveResponse(request);

                if (response == null || response[0] == (byte) 0xFF) {
                    // HSM closed session without sending any response!!
                    repairConnection(connector);
                } else {

                    response = Message.takeCommonHeader(response, Message.HVN, Message.createMessageID(id, 2));
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    if (ESMDriver.Meta_Function) {
                        response = Message.takeMetaFunctionHeader(response, Message.MTI_01, Message.MTV, Message.createMessageID(id, 4));
                        if (response.length <= 2) {
                            logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                            releaseConnection(connector);
                            available.release();
                            return null;
                        }
                    }

                    response = Message.takeFuncCode(response, FunctionCode.IT_KEY_GEN, false  true );
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    releaseConnection(connector);
                    available.release();
                    return response;
                }
            }
        } catch (InterruptedException e) {
            logger.error("Error: availabe semaphor cannot be acquired", e);
        }

        available.release();
    */
    	return null;
    	}

    /*
      * Data Ciphering Functions 1)encrypt ENCIPHER_2 EE0800 2)decrypt DECIPHER_2 EE0801
      */

    public byte[] encrypt(byte[] keyData, int index, int mode, byte[] iv, byte[] data, int padding) {/*
        try {
            available.acquire();

            Connector connector = getFreeConnection();
            DefaultHSMHandler handler = (DefaultHSMHandler) connector.getHandler();
            if (handler != null) {

                byte format = (byte) 0x00;

                if (keyData == null) {
                    // hsm-stored key
                    if (0 <= index && index < 100) {
                        format = (byte) 0x00;
                    } else if (100 <= index && index < 1000)
                        format = (byte) 0x03;
                } else {
                    if (keyData.length == 8) {
                        format = (byte) 0x10;
                        // System.out.println("Key: Format " + 10);
                    } else if (keyData.length == 16) {
                        // format = (byte) 0x11;
                        
                               * Note algorithm =0 => ISO -> ECB => format 11 algorithm =1 => ISO -> CBC => format 13
                               
                        format = (mode == 0) ? (byte) 0x11 : (byte) 0x13;
                    }
                }

                KeySpecifier key = getKey(format, index, keyData);

                byte[] request = DataCiphering.Encipher_2(key, mode, iv, data, padding);

                int id = 0;
                request = Message.putFuncCode(request, FunctionCode.ENCIPHER_2, (byte) 0x00, false);
                if (ESMDriver.Meta_Function)
                    request = Message.putMetaFunctionHeader(request, Message.MTI_01, Message.MTV, Message.createMessageID(id, 4));
                request = Message.putCommonHeader(request, Message.HVN, Message.createMessageID(id, 2));

                byte[] response = handler.sendMessageReceiveResponse(request);
                if (response == null || response[0] == (byte) 0xFF) {
                    // HSM closed session without sending any response!!
                    repairConnection(connector);
                } else {

                    response = Message.takeCommonHeader(response, Message.HVN, Message.createMessageID(id, 2));
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    if (ESMDriver.Meta_Function) {
                        response = Message.takeMetaFunctionHeader(response, Message.MTI_01, Message.MTV, Message.createMessageID(id, 4));
                        if (response.length <= 2) {
                            logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                            releaseConnection(connector);
                            available.release();
                            return null;
                        }
                    }
                    response = Message.takeFuncCode(response, FunctionCode.IT_KEY_GEN, false  true );
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    releaseConnection(connector);
                    available.release();
                    return response;
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    */
    	return null;
    	}

    public byte[] decrypt(byte[] keyData, int index, int mode, byte[] iv, byte[] data, int padding) {/*

        try {
            available.acquire();

            Connector connector = getFreeConnection();
            DefaultHSMHandler handler = (DefaultHSMHandler) connector.getHandler();
            if (handler != null) {

                byte format = (byte) 0x00;

                if (keyData == null) {
                    // hsm-stored key
                    if (0 <= index && index < 100) {
                        format = (byte) 0x00;
                    } else if (100 <= index && index < 1000)
                        format = (byte) 0x03;
                } else {
                    if (keyData.length == 8) {
                        format = (byte) 0x10;
                        // System.out.println("Key: Format " + 10);
                    } else if (keyData.length == 16) {
                        // format = (byte) 0x13;
                        
                               * Note algorithm =0 => ISO -> ECB => format 11 algorithm =1 => ISO -> CBC => format 13
                               
                        format = (mode == 0) ? (byte) 0x11 : (byte) 0x13;
                    }
                }

                KeySpecifier key = getKey(format, index, keyData);

                byte[] request = DataCiphering.Decipher_2(key, mode, iv, data, padding);

                int id = 0;
                request = Message.putFuncCode(request, FunctionCode.DECIPHER_2, (byte) 0x00, false);
                if (ESMDriver.Meta_Function)
                    request = Message.putMetaFunctionHeader(request, Message.MTI_01, Message.MTV, Message.createMessageID(id, 4));
                request = Message.putCommonHeader(request, Message.HVN, Message.createMessageID(id, 2));

                byte[] response = handler.sendMessageReceiveResponse(request);
                if (response == null || response[0] == (byte) 0xFF) {
                    // HSM closed session without sending any response!!
                    repairConnection(connector);
                } else {

                    response = Message.takeCommonHeader(response, Message.HVN, Message.createMessageID(id, 2));
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    if (ESMDriver.Meta_Function) {
                        response = Message.takeMetaFunctionHeader(response, Message.MTI_01, Message.MTV, Message.createMessageID(id, 4));
                        if (response.length <= 2) {
                            logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                            releaseConnection(connector);
                            available.release();
                            return null;
                        }
                    }
                    response = Message.takeFuncCode(response, FunctionCode.IT_KEY_GEN, false  true );
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    releaseConnection(connector);
                    available.release();
                    return response;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    */
    	return null;
    	}

    /*
      * Interchange Functions 1)Initial Session Key Generation : generateInitialSessionKeys II_KEY_GEN EE0402
      */

    public byte[] generateInitialSessionKeys(int keyIndex, byte[] keyData, int mode, int[] keyFlags) {/*
        try {
            available.acquire();

            Connector connector = getFreeConnection();
            DefaultHSMHandler handler = (DefaultHSMHandler) connector.getHandler();

            if (handler != null) {
                byte format = getFormat(keyIndex, keyData, mode);

                KeySpecifier keySpec = getKey(format, keyIndex, keyData);

                int id = 25;
                byte[] request;

                request = InterchangeMgr.II_KEY_GEN(keySpec, keyFlags, mode);
                request = Message.putFuncCode(request, FunctionCode.II_KEY_GEN, (byte) 0x00, ESMDriver.Fixed_Length);
                if (ESMDriver.Meta_Function)
                    request = Message.putMetaFunctionHeader(request, Message.MTI_01, Message.MTV, Message.createMessageID(id, 4));
                request = Message.putCommonHeader(request, Message.HVN, Message.createMessageID(id, 2));

                byte[] response;
                response = handler.sendMessageReceiveResponse(request);

                if (response == null || response[0] == (byte) 0xFF) {
                    // HSM closed session without sending any response!!
                    repairConnection(connector);
                } else {

                    response = Message.takeCommonHeader(response, Message.HVN, Message.createMessageID(id, 2));
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    if (ESMDriver.Meta_Function) {
                        response = Message.takeMetaFunctionHeader(response, Message.MTI_01, Message.MTV, Message.createMessageID(id, 4));
                        if (response.length <= 2) {
                            logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                            releaseConnection(connector);
                            available.release();
                            return null;
                        }
                    }
                    response = Message.takeFuncCode(response, FunctionCode.II_KEY_GEN, ESMDriver.Fixed_Length);
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    releaseConnection(connector);
                    available.release();
                    return response;
                }
            }
        } catch (InterruptedException e) {
            logger.error("Error: availabe semaphor cannot be acquired", e);
        }
        available.release();
    */
    	return null;
    	}


    public byte[] KEY_RCV(int encryptingKeyIndex, byte[] encryptingKeyData, int mode, int[] keyFlags, List<byte[]> keyData) {/*

        try {
            available.acquire();
            Connector connector = getFreeConnection();
            DefaultHSMHandler handler = (DefaultHSMHandler) connector.getHandler();

            if (handler != null) {

                byte format = getFormat(encryptingKeyIndex, encryptingKeyData, mode);

                KeySpecifier kirSpec = getKey(format, encryptingKeyIndex, encryptingKeyData);

                int id = 25;
                byte[] request;

                request = InterchangeMgr.II_KEY_RCV(kirSpec, keyFlags, mode, keyData);
                request = Message.putFuncCode(request, FunctionCode.II_KEY_RCV, (byte) 0x00, ESMDriver.Fixed_Length);
                if (ESMDriver.Meta_Function)
                    request = Message.putMetaFunctionHeader(request, Message.MTI_01, Message.MTV, Message.createMessageID(id, 4));
                request = Message.putCommonHeader(request, Message.HVN, Message.createMessageID(id, 2));

                byte[] response;
                response = handler.sendMessageReceiveResponse(request);

                if (response == null || response[0] == (byte) 0xFF) {
                    // HSM closed session without sending any response!!
                    repairConnection(connector);
                } else {

                    response = Message.takeCommonHeader(response, Message.HVN, Message.createMessageID(id, 2));
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    if (ESMDriver.Meta_Function) {
                        response = Message.takeMetaFunctionHeader(response, Message.MTI_01, Message.MTV, Message.createMessageID(id, 4));
                        if (response.length <= 2) {
                            logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                            releaseConnection(connector);
                            available.release();
                            return null;
                        }
                    }
                    response = Message.takeFuncCode(response, FunctionCode.II_KEY_RCV, ESMDriver.Fixed_Length);
                    if (response.length <= 2) {
                        logger.warn("An Error was occured: " + ErrorCode.getDescription(response[1]));
                        releaseConnection(connector);
                        available.release();
                        return null;
                    }

                    releaseConnection(connector);
                    available.release();
                    return response;
                }
            }
        } catch (InterruptedException e) {
            logger.error("Error: availabe semaphor cannot be acquired", e);
        }
        available.release();
    */
    	return null;
    	}

    private byte getFormat(int keyIndex, byte[] keyData, int mode) {
        // TODO format of key must be clear!!
        // if KeyData = null then the key must be hsm-stored key (00, 01,02, 03)

        byte format = (byte) 0x00;

        if (keyData == null) {
            // HSM-stored key
            if (0 <= keyIndex && keyIndex < 100) {
                format = (byte) 0x00;
            } else if (100 <= keyIndex && keyIndex < 1000)
                format = (byte) 0x03;
        } else {
            if (keyData.length == 8) {
                format = (byte) 0x10;
                // System.out.println("Key: Format " + 10);
            } else if (keyData.length == 16) {
                /*
                 * Note format-11 ECB format-13 CBC
                 */
                format = (CryptoMode.ECB == mode) ? (byte) 0x11 : (byte) 0x13;
                // System.out.println("Key: Format " + format);
            }
        }
        return format;
    }

    private KeySpecifier getKey(byte format, int index, byte[] data) {
        KeySpecifier key = null;
        switch (format) {
            case (byte) 0x00:
                key = new KeySpec00(index);
                break;
            case (byte) 0x01:
                key = new KeySpec01(index);
                break;
            case (byte) 0x02:
                key = new KeySpec02(index);
                break;
            case (byte) 0x03:
                key = new KeySpec03(index);
                break;
            case (byte) 0x10:
                key = new KeySpec10(data);
                break;
            case (byte) 0x11:
                key = new KeySpec11(data);
                break;

            case (byte) 0x13:
                key = new KeySpec13(data);
                break;
            case (byte) 0x14:
                key = new KeySpec14(data);
                break;
                // case (byte) 0x15:
                // key = new KeySpec15();
                // break;
            default:
                break;
        }

        return key;

    }

    class SessionClosedException extends Exception {

    }
}

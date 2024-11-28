package vaulsys.webservice.walletcardmgmtwebservice.handler;

import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.util.Util;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.Service;

import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.net.URL;

/**
 * Created by Raza Murtaza on 12-Mar-18.
 */
public class NayaPayBankServiceCaller {

    private static final Logger logger = Logger.getLogger(NayaPayBankServiceCaller.class);
    public static String soapEndpointUrl, soapAction;


    public static WalletCMSWsEntity CallCreateWalletLevelZero(WalletCMSWsEntity npwsentity)
    {
        try {


            //Raza start
            URL url = new URL("http://192.168.0.105:8023/NayaPayWebService?wsdl");
            QName qname = new QName("http://webservice.nayapaywebservice/", "NayaPayWebServiceImplService");

            Service service = Service.create(url, qname);

            //NayaPayWebServiceImpl npwsent = service.getPort(NayaPayWebServiceImpl.class);

            //WalletCMSWsEntity resp = npwsent.createWalletLevelZero(npwsentity);
            //System.out.println("Reply from Bank Soap ["+ resp.getRespcode() + "]");

            //HelloWorld hello = service.getPort(HelloWorld.class);

            //System.out.println(hello.getHelloWorldAsString("mkyong"));
            //Raza end


//
//            soapEndpointUrl = "http://192.168.0.105:8023/NayaPayWebService";
//            //soapAction = "http://tempuri.org/CreateWalletLevelZero";
//            soapAction = "http://webservice.nayapaywebservice/NayaPayWebServiceImpl/CreateWalletLevelZeroRequest";
//
//            String responseCode = callSoapWebService(soapEndpointUrl, soapAction, npwsentity);
//
//            if (!Util.hasText(responseCode)) {
//                npwsentity.setRespcode(ISOResponseCodes.ERROR_GENERALERROR);
//            }
//            npwsentity.setRespcode(ISOResponseCodes.APPROVED);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            npwsentity.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 Unable to Process ; Refer to Doc
            return npwsentity;
        }
        return npwsentity;
    }

    private static String callSoapWebService(String soapEndpointUrl, String soapAction, WalletCMSWsEntity npwsentity) throws Exception {
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            logger.info("Calling webservice for Real-Time Feed Transaction");

            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(soapAction, npwsentity), soapEndpointUrl);

            // Print the SOAP Response
            logger.info("Receive response of Real-Time Feed Transaction");
            NodeList nodeList = soapResponse.getSOAPBody().getElementsByTagName("TransactionResponse");
            String response = (nodeList.item(0) != null) ? nodeList.item(0).getTextContent() : "";

            if (Util.hasText(response))
                response = response.substring(response.indexOf("ResponseCode")+13, response.indexOf("ResponseCode")+15);

            if (Util.hasText(response) && response.equals(ISOResponseCodes.APPROVED))
                logger.info("Successful response received for Real-Time Feed Transaction");
            else
                logger.error("Unsuccessful response received for Real-Time Feed Transaction");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapResponse.writeTo(out);
            logger.info(new String(out.toByteArray()));

            soapConnection.close();

            return response;
        } catch (Exception e) {
            logger.error("Error occurred while sending SOAP Request to Server! Make sure you have the correct endpoint URL and SOAPAction!");
            throw e;
        }
    }

    private static SOAPMessage createSOAPRequest(String soapAction, WalletCMSWsEntity ifx) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        logger.info("Creating SOAP request for Real-Time Feed Transaction");
        createSoapEnvelope(soapMessage, ifx);

//        MimeHeaders headers = soapMessage.getMimeHeaders();
//        headers.addHeader("SOAPAction", soapAction);
//        headers.addHeader("Content-Type", "text/xml; charset=utf-8");
//        headers.addHeader("length", "2204");

        soapMessage.saveChanges();

        /* Print the request message, just for debugging purposes */
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        soapMessage.writeTo(out);
        logger.info(new String(out.toByteArray()));

        return soapMessage;
    }

    private static void createSoapEnvelope(SOAPMessage soapMessage, WalletCMSWsEntity ifx) throws SOAPException {

        logger.info("Creating SOAP Envelope for Real-Time Feed Transaction");

        String myNamespace, myNamespaceURI, value, gXSIServerURI, gXSDServerURI, scheme;
        SOAPPart soapPart;
        SOAPEnvelope envelope;
        SOAPHeader soapHeader;
        SOAPBody soapBody;
        SOAPElement soapElemTran, soapElemObjTran;//, soapElemUserCred;

        myNamespace = "tns";
        myNamespaceURI = "http://tempuri.org/";
        gXSIServerURI = "http://www.w3.org/2001/XMLSchema-instance";
        gXSDServerURI = "http://www.w3.org/2001/XMLSchema";

        // SOAP Envelope
        soapPart = soapMessage.getSOAPPart();
        envelope = soapPart.getEnvelope();
        //envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);
        envelope.removeNamespaceDeclaration(envelope.getPrefix());
        envelope.addNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        envelope.setPrefix("soap");
        envelope.addNamespaceDeclaration("xsi", gXSIServerURI);
        envelope.addNamespaceDeclaration("xsd", gXSDServerURI);

        //get rid of empty header
        soapHeader = envelope.getHeader();
        soapHeader.detachNode();

        //setting prefix to soap
        //envelope.setPrefix("soap");

            /*
            Constructed SOAP Request Message:
            <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:myNamespace="https://www.w3schools.com/xml/">
                <SOAP-ENV:Header/>
                <SOAP-ENV:Body>
                    <myNamespace:CelsiusToFahrenheit>
                        <myNamespace:Celsius>100</myNamespace:Celsius>
                    </myNamespace:CelsiusToFahrenheit>
                </SOAP-ENV:Body>
            </SOAP-ENV:Envelope>
            */

        // SOAP Body
        soapBody = envelope.getBody();
        soapBody.setPrefix("soap");

        //transaction tag
        soapElemTran = soapBody.addChildElement("CreateWalletLevelZero", "", myNamespaceURI);

        //objUserCredential tag
//        soapElemUserCred = soapElemTran.addChildElement("objUserCredential");
//        soapElemUserCred.addChildElement("UserName").addTextNode("euronet");
//        soapElemUserCred.addChildElement("Password").addTextNode("12345");

        //objTransaction tag
        soapElemObjTran = soapElemTran.addChildElement("arg0");

        //objTransaction child elements
        value = "123456789";
        soapElemObjTran.addChildElement("accountnumber").addTextNode(value);
    }




}

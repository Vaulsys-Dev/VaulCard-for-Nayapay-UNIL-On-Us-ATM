
package vaulsys.webservices.najacxf.com.nrdc.rahvar.banking.bank.webservice.core;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.nrdc.rahvar.banking.bank.webservice.core package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _BankServiceException_QNAME = new QName("http://core.webservice.bank.banking.rahvar.nrdc.com/", "BankServiceException");
    private final static QName _RhvEnfPdaResponse_QNAME = new QName("http://core.webservice.bank.banking.rahvar.nrdc.com/", "RhvEnfPdaResponse");
    private final static QName _RhvPdaRequest_QNAME = new QName("http://core.webservice.bank.banking.rahvar.nrdc.com/", "RhvPdaRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.nrdc.rahvar.banking.bank.webservice.core
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RhvEnfRequest }
     * 
     */
    public RhvEnfRequest createRhvEnfRequest() {
        return new RhvEnfRequest();
    }

    /**
     * Create an instance of {@link RhvEnfResponse }
     * 
     */
    public RhvEnfResponse createRhvEnfResponse() {
        return new RhvEnfResponse();
    }

    /**
     * Create an instance of {@link BankServiceException }
     * 
     */
    public BankServiceException createBankServiceException() {
        return new BankServiceException();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BankServiceException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://core.webservice.bank.banking.rahvar.nrdc.com/", name = "BankServiceException")
    public JAXBElement<BankServiceException> createBankServiceException(BankServiceException value) {
        return new JAXBElement<BankServiceException>(_BankServiceException_QNAME, BankServiceException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RhvEnfResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://core.webservice.bank.banking.rahvar.nrdc.com/", name = "RhvEnfPdaResponse")
    public JAXBElement<RhvEnfResponse> createRhvEnfPdaResponse(RhvEnfResponse value) {
        return new JAXBElement<RhvEnfResponse>(_RhvEnfPdaResponse_QNAME, RhvEnfResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RhvEnfRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://core.webservice.bank.banking.rahvar.nrdc.com/", name = "RhvPdaRequest")
    public JAXBElement<RhvEnfRequest> createRhvPdaRequest(RhvEnfRequest value) {
        return new JAXBElement<RhvEnfRequest>(_RhvPdaRequest_QNAME, RhvEnfRequest.class, null, value);
    }

}

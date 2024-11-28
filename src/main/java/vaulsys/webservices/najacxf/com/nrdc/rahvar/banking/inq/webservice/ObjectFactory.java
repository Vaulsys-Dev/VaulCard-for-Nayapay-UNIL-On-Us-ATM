
package vaulsys.webservices.najacxf.com.nrdc.rahvar.banking.inq.webservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.nrdc.rahvar.banking.inq.webservice package. 
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

    private final static QName _RequestTitle_QNAME = new QName("http://webservice.inq.banking.rahvar.nrdc.com/", "requestTitle");
    private final static QName _Params_QNAME = new QName("http://webservice.inq.banking.rahvar.nrdc.com/", "params");
    private final static QName _RequestStatus_QNAME = new QName("http://webservice.inq.banking.rahvar.nrdc.com/", "requestStatus");
    private final static QName _RequestId_QNAME = new QName("http://webservice.inq.banking.rahvar.nrdc.com/", "requestId");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.nrdc.rahvar.banking.inq.webservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.inq.banking.rahvar.nrdc.com/", name = "requestTitle")
    public JAXBElement<String> createRequestTitle(String value) {
        return new JAXBElement<String>(_RequestTitle_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.inq.banking.rahvar.nrdc.com/", name = "params")
    public JAXBElement<String> createParams(String value) {
        return new JAXBElement<String>(_Params_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.inq.banking.rahvar.nrdc.com/", name = "requestStatus")
    public JAXBElement<String> createRequestStatus(String value) {
        return new JAXBElement<String>(_RequestStatus_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.inq.banking.rahvar.nrdc.com/", name = "requestId")
    public JAXBElement<String> createRequestId(String value) {
        return new JAXBElement<String>(_RequestId_QNAME, String.class, null, value);
    }

}

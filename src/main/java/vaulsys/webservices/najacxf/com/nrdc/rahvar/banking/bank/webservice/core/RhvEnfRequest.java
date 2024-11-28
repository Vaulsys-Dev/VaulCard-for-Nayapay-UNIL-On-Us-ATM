
package vaulsys.webservices.najacxf.com.nrdc.rahvar.banking.bank.webservice.core;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for rhvEnfRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="rhvEnfRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://webservice.inq.banking.rahvar.nrdc.com/}params" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://webservice.inq.banking.rahvar.nrdc.com/}requestId" minOccurs="0"/>
 *         &lt;element ref="{http://webservice.inq.banking.rahvar.nrdc.com/}requestStatus" minOccurs="0"/>
 *         &lt;element ref="{http://webservice.inq.banking.rahvar.nrdc.com/}requestTitle" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rhvEnfRequest", propOrder = {
    "params",
    "requestId",
    "requestStatus",
    "requestTitle"
})
public class RhvEnfRequest {

    @XmlElement(namespace = "http://webservice.inq.banking.rahvar.nrdc.com/")
    protected List<String> params;
    @XmlElement(namespace = "http://webservice.inq.banking.rahvar.nrdc.com/")
    protected String requestId;
    @XmlElement(namespace = "http://webservice.inq.banking.rahvar.nrdc.com/")
    protected String requestStatus;
    @XmlElement(namespace = "http://webservice.inq.banking.rahvar.nrdc.com/")
    protected String requestTitle;

    /**
     * Gets the value of the params property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the params property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParams().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getParams() {
        if (params == null) {
            params = new ArrayList<String>();
        }
        return this.params;
    }
    
    public void setParams(String[] str) {
    	
            params = new ArrayList<String>();
            for(String s:str){
            	params.add(s);
            }
    }

    /**
     * Gets the value of the requestId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the value of the requestId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestId(String value) {
        this.requestId = value;
    }

    /**
     * Gets the value of the requestStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestStatus() {
        return requestStatus;
    }

    /**
     * Sets the value of the requestStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestStatus(String value) {
        this.requestStatus = value;
    }

    /**
     * Gets the value of the requestTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestTitle() {
        return requestTitle;
    }

    /**
     * Sets the value of the requestTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestTitle(String value) {
        this.requestTitle = value;
    }

}

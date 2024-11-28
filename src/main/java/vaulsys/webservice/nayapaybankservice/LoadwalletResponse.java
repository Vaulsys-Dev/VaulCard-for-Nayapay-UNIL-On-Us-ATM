
package vaulsys.webservice.nayapaybankservice;

import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for loadwalletResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="loadwalletResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://nayapaybankservice/}nayaPayWsEntity" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "loadwalletResponse", propOrder = {
    "_return"
})
public class LoadwalletResponse {

    @XmlElement(name = "return")
    protected WalletCMSWsEntity _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link WalletCMSWsEntity }
     *     
     */
    public WalletCMSWsEntity getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link WalletCMSWsEntity }
     *     
     */
    public void setReturn(WalletCMSWsEntity value) {
        this._return = value;
    }

}

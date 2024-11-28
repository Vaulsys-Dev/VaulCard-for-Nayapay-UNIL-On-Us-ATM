
package vaulsys.webservice.nayapaybankservice;

import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for loadwallet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="loadwallet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="arg0" type="{http://nayapaybankservice/}nayaPayWsEntity" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "loadwallet", propOrder = {
    "arg0"
})
public class Loadwallet {

    protected WalletCMSWsEntity arg0;

    /**
     * Gets the value of the arg0 property.
     * 
     * @return
     *     possible object is
     *     {@link WalletCMSWsEntity }
     *     
     */
    public WalletCMSWsEntity getArg0() {
        return arg0;
    }

    /**
     * Sets the value of the arg0 property.
     * 
     * @param value
     *     allowed object is
     *     {@link WalletCMSWsEntity }
     *     
     */
    public void setArg0(WalletCMSWsEntity value) {
        this.arg0 = value;
    }

}

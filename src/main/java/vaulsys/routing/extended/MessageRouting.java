package vaulsys.routing.extended;

import vaulsys.persistence.IEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by m.rehman on 4/14/2016.
 * This class is used to Load Routing Entries from DB
 */
@Entity
@Table(name = "msg_routing")
public class MessageRouting implements IEntity<Long> {


    /*@GeneratedValue(generator = "routing-seq-gen") //Raza commenitng not required as record will never be inserted by core
    @org.hibernate.annotations.GenericGenerator(name = "routing-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "msg_routing_seq")
            })*/
    //Note: Raza Adding Old Column in order to work with transactions from POS,ATM etc. as they use TrnType.. instead of TranCode.. etc
    @Id
    private Long id;

    @Column(name = "CHANNEL_NAME")
    private String ChannelName;

    @Column(name = "BIN")
    private String bin;

    @Column(name = "MSG_TYPE")
    private String mti;

    @Column(name = "INSTITUTION_ID")
    private String InstitutionId;

    @Column(name = "TRAN_CODE")
    private String TranCode;

    @Column(name = "ENTITY_TYPE")
    private String TerminalType;

    @Column(name = "SERVICE")
    private String TranType;

    @Column(name = "CARD_ISSUER")
    private String DestBankId;

    @Column(name = "CARD_ACQUIRER")
    private String BankId;

    @Column(name = "CARD_RECEIVER")
    private String RecBankId;

    //@Column(name = "IP_ADDRESS") //Raza commenting
    //private String referenceInstId; //Raza commenting

    @Column(name = "DESTINATION")
    private String destination;

    public MessageRouting() {
        id = 0L;
        ChannelName = "";
        bin = "";
        mti = "";
        InstitutionId = "";
        //referenceInstId = "";
        TranCode = "";
        TerminalType = "";
        TranType = "";
        DestBankId = "";
        BankId = "";
        RecBankId = "";
        destination = "";
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelName()
    {
        return this.ChannelName;
    }

    public void setChannelName(String channelname)
    {
        this.ChannelName = channelname;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }


    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public String getInstitutionId()
    {
        return this.InstitutionId;
    }

    public void setInstitutionId(String InsId)
    {
        this.InstitutionId = InsId;
    }

    public String getTranCode()
    {
        return this.TranCode;
    }

    public void setTranCode(String trancode)
    {
        this.TranCode = trancode;
    }

    public String getTerminalType()
    {
        return this.TerminalType;
    }

    public void setTerminalType(String termtype)
    {
        this.TerminalType = termtype;
    }

    public String getTranType()
    {
        return this.TranType;
    }

    public void setTranType(String trntype)
    {
        this.TranType = trntype;
    }

    public String getDestBankId()
    {
        return this.DestBankId;
    }

    public void setDestBankId(String destbnk)
    {
        this.DestBankId = destbnk;
    }

    public String getBankId()
    {
        return this.BankId;
    }

    public void setBankId(String bnkid)
    {
        this.BankId = bnkid;
    }

    public String getRecBankId()
    {
        return this.RecBankId;
    }

    public void setRecBankId(String recbnkid)
    {
        this.RecBankId = recbnkid;
    }

    /*public String getReferenceInstId() { //Raza commenting
          return referenceInstId;
    }

    public void setReferenceInstId(String referenceInstId) {
        this.referenceInstId = referenceInstId;
    }*/

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}

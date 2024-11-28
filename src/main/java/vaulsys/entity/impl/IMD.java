//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package vaulsys.entity.impl;

import vaulsys.config.CHANNELS;
import vaulsys.config.IMDType;
import vaulsys.mtn.util.irancell.hibernate.HibernateUtil;
import vaulsys.persistence.BaseEntity;
import vaulsys.persistence.IEntity;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "config_bin")
@PrimaryKeyJoinColumn(name = "IMD")

public class IMD extends BaseEntity<String> {
    @Id
    private String IMD;
    private String Bank_Ack;
    private String Bank_Name;

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "IMD_TYPE", column = @Column(name = "IMD_Type"))})
    private IMDType IMD_Type;

    //@Embedded
    //@AttributeOverrides({@AttributeOverride(name = "CHANNEL_ID", column = @Column(name = "CHANNEL_ID"))})
    private String CHANNEL_ID;

    private boolean IS_ENABLED = true;

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "NETWORK_TYPE", column = @Column(name = "NETWORK_TYPE"))})
    private NetworkType NETWORK_TYPE;

    private boolean BASE_IMD = true;

    @Transient
    private String Op_Flag;

    // Asim Shahzad, Date : 22nd Sep 2020, Tracking ID : VC-NAP-202009101 / VP-NAP-202009104
    @Column(name = "card_scheme")
    private String cardScheme;
    // =====================================================================================


    public IMD() {
    }

    public String getId() {
        return this.IMD;
    }

    public void setId(String IMD) {
        this.IMD = IMD;
    }

    public String getIMD()
    {
        return this.IMD;
    }

    public void setIMD(String IMD)
    {
        this.IMD = IMD;
    }

    public String getOp_Flag() {
        return this.Op_Flag;
    }

    public void setOp_Flag(String Op_Flag) {
        this.Op_Flag = Op_Flag;
    }

    public boolean getBASE_IMD() {
        return this.BASE_IMD;
    }

    public void setBASE_IMD(boolean REF_IMD) {
        this.BASE_IMD = REF_IMD;
    }

    public IMDType getIMD_Type() {
        return IMD_Type;
    }

    public void setIMD_Type(IMDType IMD_Type) {
        this.IMD_Type = IMD_Type;
    }

    // Asim Shahzad, Date : 22nd Sep 2020, Tracking ID : VC-NAP-202009101 / VP-NAP-202009104

    public String getCardScheme() {
        return cardScheme;
    }

    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    // =====================================================================================

    @Override
    public String toString() {
        return String.format("%s", this.IMD);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof IMD))
            return false;
        IMD that = (IMD) o;
        return getId().equals(that.getId());
    }
}

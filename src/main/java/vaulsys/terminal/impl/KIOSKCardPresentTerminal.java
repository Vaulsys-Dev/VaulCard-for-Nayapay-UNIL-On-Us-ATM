package vaulsys.terminal.impl;

import vaulsys.entity.impl.Branch;
import vaulsys.entity.impl.Shop;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.atm.ATMConnectionStatus;
import org.apache.log4j.Logger;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "term_kiosk_cardpresent")
@ForeignKey(name = "kioskcardpresent_terminal_fk")
//@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class KIOSKCardPresentTerminal extends Terminal {
    @Transient
    private transient Logger logger = Logger.getLogger(this.getClass());

    @Embedded
    @AttributeOverride(name = "status", column = @Column(name = "connection"))
    private ATMConnectionStatus connection = ATMConnectionStatus.NOT_CONNECTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch")
    @ForeignKey(name = "branch_fk")
    private Branch branch;

    @Column(name = "branch", insertable = false, updatable = false)
    private Long branchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    @ForeignKey(name = "kioskcardpresent_owner_fk")
    private Shop owner;

    @Column(name = "owner", insertable = false, updatable = false)
    private Long ownerId;

    public Long getOwnerId() {
        return ownerId;
    }

    /******** Kiosk card present Terminal Version Properties ********/
    /**
     * ***** Start *******
     */
    @Column(length = 15)
    private String IP;

    @Column(length = 20)
    private String serialno;


    public String getSerialno() {
        return serialno;
    }

    public void setSerialno(String serialno) {
        this.serialno = serialno;
    }

    @Column(length = 50)
    private String description;


    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }


    /******** End ********/
    /**
     * ***** Kiosk card present Terminal Version Properties *******
     */


    public KIOSKCardPresentTerminal() {
    }

    public KIOSKCardPresentTerminal(Long code) {
        super(code);
    }


    @Override
    public Shop getOwner() {
        return owner;
    }

    @Override
    public TerminalType getTerminalType() {
        return TerminalType.KIOSK_CARD_PRESENT;
    }

    public void setOwner(Shop owner) {
        this.owner = owner;
        if (owner != null)
            this.ownerId = owner.getId();
    }

    public TerminalClearingMode getClearingMode() {
        return TerminalClearingMode.TERMINAL;
    }

    public Long getBranchId() {
        return branchId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((IP == null) ? 0 : IP.hashCode());
        result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
        result = prime * result + ((branchId == null) ? 0 : branchId.hashCode());
        return result;
    }

    public void setConnection(ATMConnectionStatus connection) {
        this.connection = connection;
    }

    public ATMConnectionStatus getConnection() {
        return connection;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
        if (branch != null)
            this.branchId = branch.getId();
    }


}

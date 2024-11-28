package vaulsys.terminal.impl;

import vaulsys.entity.impl.Branch;
import vaulsys.entity.impl.Shop;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.TerminalStatus;
import vaulsys.terminal.atm.ATMConnectionStatus;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: Sahar-hoseini-PC
 * Date: 1/28/13
 * Time: 2:37 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "term_kiosk_card_present_ver")
public class KIOSKCardPresentTerminalVersion extends TerminalVersion {

    @ManyToOne
    @JoinColumn(name = "parent")
    @ForeignKey(name = "kiosk_card_present_vers_parent_fk")
    private KIOSKCardPresentTerminal parent;

    @Embedded
    @AttributeOverride(name = "status", column = @Column(name = "connection"))
    private ATMConnectionStatus connection = ATMConnectionStatus.NOT_CONNECTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    @ForeignKey(name = "kioskcardpresent_owner_fk")
    private Shop owner;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch")
	@ForeignKey(name = "branch_fk")
	private Branch branch;

    @Column(name = "owner", insertable = false, updatable = false)
    private Long ownerId;

    @Embedded
    @AttributeOverride(name = "status", column = @Column(name = "status"))
    TerminalStatus status = TerminalStatus.NOT_INSTALL;

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

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

/******** End ********/
    /**
     * ***** Kiosk card present Terminal Version Properties *******
     */


    public KIOSKCardPresentTerminalVersion() {
    }

    public void setOwner(Shop owner) {
        this.owner = owner;
    }

    public TerminalClearingMode getClearingMode() {
        return TerminalClearingMode.TERMINAL;
    }

    public KIOSKCardPresentTerminal getParent() {
        return parent;
    }

    public void setParent(KIOSKCardPresentTerminal parent) {
        this.parent = parent;
    }

    public ATMConnectionStatus getConnection() {
        return connection;
    }

    public void setConnection(ATMConnectionStatus connection) {
        this.connection = connection;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TerminalStatus getStatus() {
        return status;
    }

    public void setStatus(TerminalStatus status) {
        this.status = status;
    }
}

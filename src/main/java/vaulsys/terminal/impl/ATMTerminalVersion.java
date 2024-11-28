package vaulsys.terminal.impl;

import vaulsys.entity.impl.Branch;
import vaulsys.terminal.TerminalStatus;
import vaulsys.terminal.atm.ATMConfiguration;
import vaulsys.terminal.atm.ATMProducer;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "term_atm_ver")
public class ATMTerminalVersion extends TerminalVersion {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    @ForeignKey(name = "atm_vers_owner_fk")
    private Branch owner;

    @ManyToOne
    @JoinColumn(name = "parent")
    @ForeignKey(name = "atm_vers_parent_fk")
    private ATMTerminal parent;

    @Column(length = 15)
    private String IP;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config")
    @ForeignKey(name = "atm_vers_config_fk")
    private ATMConfiguration configuration;

    @Column(length = 50)
    private String description;

    @Embedded
    @AttributeOverride(name = "type", column = @Column(name = "producer"))
    ATMProducer producer;

    @Column
    private Boolean changeKey;

    private Boolean setDefaultKey = false;

    @Column(name = "lastkeychange_dt")
    private Long lastKeyChangeDateLong;

    @Embedded
    @AttributeOverride(name = "status", column = @Column(name = "status"))
    TerminalStatus status = TerminalStatus.NOT_INSTALL;
//    public IVersion clone() {
//        ATMTerminalVersion version = new ATMTerminalVersion();
//        version.validRange = validRange.clone();
//        version.IP = IP;
//        version.configuration = configuration;
//        return version;
//    }

    public Boolean getChangeKey() {
        return changeKey;
    }

    public void setChangeKey(Boolean changeKey) {
        this.changeKey = changeKey;
    }

    public ATMProducer getProducer() {
        return producer;
    }

    public void setProducer(ATMProducer producer) {
        this.producer = producer;
    }

    public Branch getOwner() {
        return owner;
    }

    public void setOwner(Branch owner) {
        this.owner = owner;
    }

    public ATMTerminal getParent() {
        return parent;
    }

    public void setParent(ATMTerminal parent) {
        this.parent = parent;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public ATMConfiguration getOwnOrParentConfiguration() {
        if (configuration != null)
            return configuration;
        if (sharedFeature != null)
            return sharedFeature.getConfiguration();
        return null;
    }

    public ATMConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ATMConfiguration configuration) {
        this.configuration = configuration;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getSetDefaultKey() {
        return setDefaultKey;
    }

    public void setSetDefaultKey(Boolean setDefaultKey) {
        this.setDefaultKey = setDefaultKey;
    }

    public Long getLastKeyChangeDateLong() {
        return lastKeyChangeDateLong;
    }

    public void setLastKeyChangeDateLong(Long lastKeyChangeDateLong) {
        this.lastKeyChangeDateLong = lastKeyChangeDateLong;
    }

    public TerminalStatus getStatus() {
        return status;
    }

    public void setStatus(TerminalStatus status) {
        this.status = status;
    }
}

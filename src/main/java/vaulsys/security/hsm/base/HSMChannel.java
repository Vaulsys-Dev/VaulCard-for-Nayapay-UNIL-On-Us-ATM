package vaulsys.security.hsm.base;

import vaulsys.network.channel.base.Channel;

import javax.persistence.*;

@Entity
@Table(name = "network_info")
public class HSMChannel extends Channel {

    @Transient
    private HSMConnector connector;

    @Column(name = "BIN")
    private String bin;

    @Column(name = "TIMEOUT",insertable = false, updatable = false)
    private Long timeoutMilliSeconds;

    @Transient
    private CommandType commandType;

    @Column(name = "COMMAND_TYPE")
    private String commandTypeDesc;

    public HSMChannel() {
    }

    public HSMChannel(String bin, CommandType commandType, Long timeoutMilliSeconds) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        this.bin =bin;
        this.commandType=commandType;
        this.timeoutMilliSeconds = timeoutMilliSeconds;
    }

    public HSMConnector getConnector() {
        return connector;
    }

    public void setConnector(HSMConnector connector) {
        this.connector = connector;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public Long getTimeoutMilliSeconds() {
        return timeoutMilliSeconds;
    }

    public void setTimeoutMilliSeconds(Long timeoutMilliSeconds) {
        this.timeoutMilliSeconds = timeoutMilliSeconds;
    }

    public String getCommandTypeDesc() {
        return commandTypeDesc;
    }

    public void setCommandTypeDesc(String commandTypeDesc) {
        this.commandTypeDesc = commandTypeDesc;
    }

}

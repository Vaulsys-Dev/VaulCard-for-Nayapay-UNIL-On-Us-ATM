package vaulsys.loro;

import vaulsys.persistence.IEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by HP on 2/8/2017.
 */
@Entity
@Table (name="loro_config")
public class Loro implements IEntity<Integer> {

    @Id
    Integer id;

    String mti;

    @Column (name="tran_type")
    String tranType;

    @Column (name="resp_code")
    String respCode;

    @Column (name="orig_channel")
    String origChannel;

    @Column (name="dest_channel")
    String destChannel;

    @Column (name="host_name")
    String hostName;

    @Override
    public Integer getId() {
        return null;
    }

    @Override
    public void setId(Integer id) {

    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public String getTranType() {
        return tranType;
    }

    public void setTranType(String tranType) {
        this.tranType = tranType;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getDestChannel() {
        return destChannel;
    }

    public void setDestChannel(String destChannel) {
        this.destChannel = destChannel;
    }

    public String getOrigChannel() {
        return origChannel;
    }

    public void setOrigChannel(String origChannel) {
        this.origChannel = origChannel;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}

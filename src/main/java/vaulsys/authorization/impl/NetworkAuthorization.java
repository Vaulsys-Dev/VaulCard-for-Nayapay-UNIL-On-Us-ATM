package vaulsys.authorization.impl;

import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.TrnType;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HP on 24-Oct-17.
 */
@Entity
@Table(name = "network_tran")
public class NetworkAuthorization implements IEntity<Long> {

    @Id
    private
    Long id;

    private String srcchannel;

    private String transactions;

    private String destchannel;

    @Transient
    public static HashMap ChannelPerm = new HashMap();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getSrcChannel() {
        return srcchannel;
    }

    public void setSrcChannel(String channel) {
        this.srcchannel = channel;
    }

    public String getTransactions() {
        return transactions;
    }

    public void setTransactions(String transactions) {
        this.transactions = transactions;
    }

    public static Boolean AuthorizeTxn(String inchannel, String outchannel, TrnType trntype)
    {

        String query = "select transactions from network_tran where srcchannel = :inchann and destchannel = :outchann";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("inchann", inchannel);
        params.put("outchann", outchannel);

        List<String> templist;
        templist = GeneralDao.Instance.executeSqlQuery(query,params);

        if(templist.size() > 0) {
            return templist.get(0).contains(trntype.toString()+","); //last element must have ,
        }
        else
        {
            return false;
        }

//        for (Object[] obj : templist) {
//            //System.out.println("Cust Object Key [" + obj[0] + "]"); //Raza TEMP
//            //System.out.println("Cust Object Value [" + obj[1] + "]"); //Raza TEMP
//            ChannelPerm.put(""+obj[0],""+obj[1]); //Map made for CustCodes
//            //CustStausMap.put(obj. getCode(), obj.Description);
//        }
    }

    public String getDestchannel() {
        return destchannel;
    }

    public void setDestchannel(String destchannel) {
        this.destchannel = destchannel;
    }
}

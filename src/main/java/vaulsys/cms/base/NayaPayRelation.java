package vaulsys.cms.base;

import vaulsys.persistence.IEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Created by Raza on 22-Oct-18.
 */
@Entity
@Table(name = "NAYAPAY_WALLET_ACCT_RELATION")
public class NayaPayRelation implements IEntity<Long> {

    @Id
    @GeneratedValue(generator="CMS_ACCOUNT_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_ACCOUNT_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_ACCOUNT_ID_SEQ")
            })
    private Long id;


    private String User_Acct_Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACC_NUMBER")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="WLLTACCT_REL_FK")
    private CMSAccount account;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getUser_Acct_Id() {
        return User_Acct_Id;
    }

    public void setUser_Acct_Id(String user_Acct_Id) {
        User_Acct_Id = user_Acct_Id;
    }

    public CMSAccount getAccount() {
        return account;
    }

    public void setAccount(CMSAccount account) {
        this.account = account;
    }



    //get customer id through account

}

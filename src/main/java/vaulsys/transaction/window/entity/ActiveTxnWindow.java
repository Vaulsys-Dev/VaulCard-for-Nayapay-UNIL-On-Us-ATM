package vaulsys.transaction.window.entity;

import vaulsys.persistence.IEntity;

import javax.persistence.*;

/**
 * Created by Raza on 04-Oct-18.
 */
@Entity
@Table(name = "actv_txn_window")
public class ActiveTxnWindow implements IEntity<Long>, Cloneable {

    @Id
    @GeneratedValue(generator="ACTVTXNWINDOW_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "ACTVTXNWINDOW_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "ACTVTXN_WINDOW_SEQ")
            })
    private Long id;


    @Column(name = "RELATION")
    private String relation;

    @Column(name = "AMOUNT")
    private String amount;

    @Column(name = "CHANNEL")
    private String channel;

    @Column(name = "TRNTYPE")
    private String trntype;

    @Column(name = "STARTTIME")
    private String starttype;

    @Column(name = "ENDTIME")
    private String endtype;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getTrntype() {
        return trntype;
    }

    public void setTrntype(String trntype) {
        this.trntype = trntype;
    }

    public String getStarttype() {
        return starttype;
    }

    public void setStarttype(String starttype) {
        this.starttype = starttype;
    }

    public String getEndtype() {
        return endtype;
    }

    public void setEndtype(String endtype) {
        this.endtype = endtype;
    }
}


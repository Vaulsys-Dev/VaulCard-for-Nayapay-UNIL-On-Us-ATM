package vaulsys.cms.base;

import vaulsys.persistence.IEntity;

import javax.persistence.*;

/**
 * Asim Shahzad, Date : 12th July 2021, Tracking ID : VC-NAP-202107121
 */
@Entity
@Table(name = "CMS_CARD_SEQ_TRACKER")
public class CMSCardSequenceTracker implements IEntity<Long> {
    @Id
    @GeneratedValue(generator="CMS_CARD_SEQ_TRACKER_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_CARD_SEQ_TRACKER_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_CARD_SEQ_TRACKER_ID_SEQ")
            })
    private Long id;

    @Column(name = "SEQ_VALUE")
    private String sequenceValue;

    @Column(name = "INSERTED_ON")
    private String insertedOn;

    @Column(name = "BIN")
    private String bin;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getSequenceValue() {
        return sequenceValue;
    }

    public void setSequenceValue(String sequenceValue) {
        this.sequenceValue = sequenceValue;
    }

    public String getInsertedOn() {
        return insertedOn;
    }

    public void setInsertedOn(String insertedOn) {
        this.insertedOn = insertedOn;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }
}

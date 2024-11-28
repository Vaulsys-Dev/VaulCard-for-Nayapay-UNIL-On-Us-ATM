package vaulsys.wallet.base.ledgers;

import org.hibernate.annotations.ForeignKey;
import vaulsys.persistence.IEntity;

import javax.persistence.*;

/**
 * Created by HP on 24/05/2019.
 */

@Entity
@Table(name = "CHARTOFACCOUNT_LEVEL")
public class ChartOfAccountLevel implements IEntity<Long> {
    @Id
    @GeneratedValue(generator="CHARTOFACCOUNT_LEVEL_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CHARTOFACCOUNT_LEVEL_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CHARTOFACCOUNT_LEVEL_ID_SEQ")
            })
    private Long id;

    @Column(name = "LEVEL_TYPE")
    private String levelType;

    @Column(name = "LEVEL_LENGTH")
    private String levelLength;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PARENT")
    @ForeignKey(name = "chartOfAccLevelSelfJoin_fk")
    private ChartOfAccountLevel parent;

    @Column(name = "SAMPLE_VALUE")
    private String sampleValue;

    @Column(name = "END_SEPERATOR")
    private String endSeperator;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getLevelType() {
        return levelType;
    }

    public void setLevelType(String levelType) {
        this.levelType = levelType;
    }

    public String getLevelLength() {
        return levelLength;
    }

    public void setLevelLength(String levelLength) {
        this.levelLength = levelLength;
    }

    public ChartOfAccountLevel getParent() {
        return parent;
    }

    public void setParent(ChartOfAccountLevel parent) {
        this.parent = parent;
    }

    public String getSampleValue() {
        return sampleValue;
    }

    public void setSampleValue(String sampleValue) {
        this.sampleValue = sampleValue;
    }

    public String getEndSeperator() {
        return endSeperator;
    }

    public void setEndSeperator(String endSeperator) {
        this.endSeperator = endSeperator;
    }

    @Override
    public String toString() {
        return "Level-" + levelType;
    }
}



package vaulsys.entity.impl;

import vaulsys.persistence.IEntity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Created by a.shehzad on 6/16/2016.
 */

@Entity
@Table(name = "importstagelogging")
@PrimaryKeyJoinColumn(name = "JOB_ID")

public class ImportStats implements IEntity<Long> {
    @Id
    private Long JOB_ID;
    private String STAGE_CODE;
    private String STAGE_DESCRIPTION;
    private String START_TIME;
    private String END_TIME;
    private String STATUS;
    private String IMP_ID;

    @Override
    public Long getId() {
        return this.JOB_ID;
    }

    @Override
    public void setId(Long id) {
        this.JOB_ID = id;
    }

    public ImportStats() {
    }

    public String getSTAGE_CODE() {
        return this.STAGE_CODE;
    }

    public void setSTAGE_CODE(String STAGE_CODE) {
        this.STAGE_CODE = STAGE_CODE;
    }

    public String getSTAGE_DESCRIPTION() {
        return this.STAGE_DESCRIPTION;
    }

    public void setSTAGE_DESCRIPTION(String STAGE_DESCRIPTION) {
        this.STAGE_DESCRIPTION = STAGE_DESCRIPTION;
    }

    public String getSTART_TIME()
    {
        return START_TIME;
    }

    public void setSTART_TIME(String START_TIME)
    {
        this.START_TIME = START_TIME;
    }

    public String getEND_TIME()
    {
        return END_TIME;
    }

    public void setEND_TIME(String END_TIME)
    {
        this.END_TIME = END_TIME;
    }

    public String getSTATUS()
    {
        return STATUS;
    }

    public void setSTATUS(String STATUS)
    {
        this.STATUS = STATUS;
    }

    public String getIMP_ID()
    {
        return IMP_ID;
    }

    public void setIMP_ID(String IMP_ID)
    {
        this.IMP_ID = IMP_ID;
    }
}

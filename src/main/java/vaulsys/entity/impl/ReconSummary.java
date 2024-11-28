package vaulsys.entity.impl;

import vaulsys.persistence.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Created by HP on 10/18/2016.
 */
@Entity
@Table(name = "recon_summary")
@PrimaryKeyJoinColumn(name = "RECON_TRACE_ID")
public class ReconSummary extends BaseEntity<String> {
    @Id
    private String RECON_TRACE_ID;
    private String JOB_ID;
    private String REPORT_DATE;
    private String REPORT_TITLE;
    private String ERROR_MSG;
    private String REPORT_CODE;

    public ReconSummary(){

    }

    public String getJOB_ID()
    {
        return JOB_ID;
    }

    public void setJOB_ID(String Recon_JobID)
    {
        this.JOB_ID = Recon_JobID;
    }

    public String getREPORT_DATE()
    {
        return REPORT_DATE;
    }

    public void setREPORT_DATE(String Recon_Rep_Date)
    {
        this.REPORT_DATE = Recon_Rep_Date;
    }

    public String getREPORT_TITLE()
    {
        return REPORT_TITLE;
    }

    public void setREPORT_TITLE(String Recon_Rep_Title)
    {
        this.REPORT_TITLE = Recon_Rep_Title;
    }

    public String getERROR_MSG()
    {
        return ERROR_MSG;
    }

    public void setERROR_MSG(String Recon_Err_Msg)
    {
        this.ERROR_MSG = Recon_Err_Msg;
    }

    public String getREPORT_CODE()
    {
        return REPORT_CODE;
    }

    public void setREPORT_CODE(String Recon_Rep_Code)
    {
        this.REPORT_CODE = Recon_Rep_Code;
    }

    @Override
    public String getId()
    {
        return RECON_TRACE_ID;
    }

    @Override
    public void setId(String Recon_TraceID)
    {
        this.RECON_TRACE_ID = Recon_TraceID;
    }
}

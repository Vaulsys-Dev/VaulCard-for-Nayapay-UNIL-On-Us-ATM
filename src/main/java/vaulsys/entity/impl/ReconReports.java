package vaulsys.entity.impl;

import vaulsys.persistence.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Created by HP on 10/17/2016.
 */
@Entity
@Table(name = "recon_config_rep")
@PrimaryKeyJoinColumn(name = "FORMAT_ID")
public class ReconReports extends BaseEntity<String> {
    @Id
    private String REP_ID;
    private String Format_ID;
    private String Rep_Code;
    private String Rep_Name;

    public ReconReports(){

    }

    public String getFormat_ID()
    {
        return this.Format_ID;
    }

    public void setFormat_ID(String FormatID)
    {
        this.Format_ID = FormatID;
    }

    public String getReport_Code()
    {
        return this.Rep_Code;
    }

    public void setReport_Code(String ReportCode)
    {
        this.Rep_Code = ReportCode;
    }

    public String getReport_Name()
    {
        return this.Rep_Name;
    }

    public void setReport_Name(String ReportName)
    {
        this.Rep_Name = ReportName;
    }

    @Override
    public String getId() {
        return this.REP_ID;
    }

    @Override
    public void setId(String ReportID) {
        this.REP_ID = ReportID;
    }
}

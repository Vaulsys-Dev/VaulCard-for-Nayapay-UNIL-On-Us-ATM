package vaulsys.entity.impl;
import vaulsys.persistence.BaseEntity;

import javax.persistence.*;

/**
 * Created by HP on 9/16/2016.
 */

@Entity
@Table(name = "recon_imp_history")
@PrimaryKeyJoinColumn(name = "JOB_ID")
public class ReconProc extends BaseEntity<String>{
    @Id
    private String Job_ID;
    private String File_Name;
    private String Import_Date;
    private boolean Status = true;

    public ReconProc(){

    }

    public String getJob_ID()
    {
        return this.Job_ID;
    }

    public void setJob_ID(String JobID)
    {
        this.Job_ID = JobID;
    }

    public String getFile_Name()
    {
        return this.File_Name;
    }

    public void setFile_Name(String FileName)
    {
        this.File_Name = FileName;
    }

    public String getImport_Date()
    {
        return this.Import_Date;
    }

    public void setImport_Date(String ImportDate)
    {
        this.Import_Date = ImportDate;
    }

    public boolean getStatus()
    {
        return this.Status;
    }

    public void setStatus(boolean ImportStatus)
    {
        this.Status = ImportStatus;
    }

    @Override
    public String getId() {
        return this.Job_ID;
    }

    @Override
    public void setId(String JobID) {
        this.Job_ID = JobID;
    }
}

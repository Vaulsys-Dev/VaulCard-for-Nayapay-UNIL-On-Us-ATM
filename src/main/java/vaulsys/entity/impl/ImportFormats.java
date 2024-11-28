package vaulsys.entity.impl;

import vaulsys.persistence.IEntity;

import javax.persistence.*;
import java.util.List;

/**
 * Created by a.shehzad on 6/5/2016.
 */

@Entity
@Table(name = "import_formats")
@PrimaryKeyJoinColumn(name = "ID")

public class ImportFormats implements IEntity<String>
{
    @Id
    @Column(name="ID")
    private String ID;

    @Column(name="IMPORTNAME")
    private String Import_Desc;

//    @Column(name="SCHEMAFILE")
//    private String Import_Schema_File;
//
//    @Column(name="IMPORTCLASS")
//    private String Import_Class;
//
//    @Column(name="OPERATIONTYPE")
//    private String OPERATIONTYPE;
//
//    @Column(name="EXECUTION_TIME")
//    private String Import_Exec_Time;
//
//    @Column(name="SRC_FILE_LOC")
//    private String Import_Src_File_Location;
//
//    @Column(name="EXP_FILE_LOC")
//    private String Import_Exp_File_Location;
//
//    @Column(name="EXP_FILE_NAME")
//    private String Import_Exp_File_Name;
//
//    @Column(name="EXP_FILE_EXT")
//    private String Import_Exp_File_Ext;

    public ImportFormats()
    {
    }

    public ImportFormats(String Import_ID, String Import_Desc)
    {
        this.ID = Import_ID;
        this.Import_Desc = Import_Desc;
    }

    public String getImport_ID()
    {
        return this.ID;
    }

    public String getImport_Desc()
    {
        return this.Import_Desc;
    }

    public void setImport_Desc(String Imp_Description)
    {
        this.Import_Desc = Imp_Description;
    }

//    public String getImport_Schema_File()
//    {
//        return this.Import_Schema_File;
//    }
//
//    public void setImport_Schema_File(String Imp_Schema_File)
//    {
//        this.Import_Schema_File = Imp_Schema_File;
//    }
//
//    public String getImport_Class()
//    {
//        return this.Import_Class;
//    }
//
//    public void setImport_Class(String Imp_Class)
//    {
//        this.Import_Class = Imp_Class;
//    }
//
//    public String getImport_Op_Type()
//    {
//        return this.OPERATIONTYPE;
//    }
//
//    public void setImport_Op_Type(String Op_Type)
//    {
//        this.OPERATIONTYPE = Op_Type;
//    }
//
//    public String getImport_Exec_Time()
//    {
//        return this.Import_Exec_Time;
//    }
//
//    public void setImport_Exec_Time(String Execution_Time)
//    {
//        this.Import_Exec_Time = Execution_Time;
//    }
//
//    public String getImport_Src_File_Location()
//    {
//        return this.Import_Src_File_Location;
//    }
//
//    public void setImport_Src_File_Location(String Source_file_location)
//    {
//        this.Import_Src_File_Location = Source_file_location;
//    }
//
//    public String getImport_Exp_File_Location()
//    {
//        return this.Import_Exp_File_Location;
//    }
//
//    public void setImport_Exp_File_Location(String Export_File_Location)
//    {
//        this.Import_Exp_File_Location = Export_File_Location;
//    }
//
//    public String getImport_Exp_File_Name()
//    {
//        return this.Import_Exp_File_Name;
//    }
//
//    public void setImport_Exp_File_Name(String Export_File_Name)
//    {
//        this.Import_Exp_File_Name = Export_File_Name;
//    }
//
//    public String getImport_File_Ext()
//    {
//        return this.Import_Exp_File_Ext;
//    }
//
//    public void setImport_Exp_File_Ext(String Export_File_Ext)
//    {
//        this.Import_Exp_File_Ext = Export_File_Ext;
//    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void setId(String Imp_ID) {
        this.ID = Imp_ID;
    }
}

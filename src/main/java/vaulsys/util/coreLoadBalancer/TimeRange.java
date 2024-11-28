package vaulsys.util.coreLoadBalancer;

import vaulsys.persistence.IEntity;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "core_time_range")
public class TimeRange implements IEntity<Long> {

    @Id
    @GeneratedValue(generator = "switch-gen")
    private Long id;
    public void setId(Long id){
        this.id = id;
    }
    public Long getId(){
        return this.id;
    }

	private String startTime;
	public void setStartTime(String startTime){
		this.startTime = startTime;
	}
	public String getStartTime(){
		return this.startTime;
	}

	private String endTime;
	public void setEndTime(String endTime){
		this.endTime = endTime;
	}
	public String getEndTime(){
		return this.endTime;
	}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "core_server")
    @ForeignKey(name="time_core_fk")
    private CoreServer coreServer;

    public CoreServer getCoreServer() {
        return coreServer;
    }

    public void setCoreServer(CoreServer coreServer) {
        this.coreServer = coreServer;
    }
}


/*
CREATE TABLE "time-range-core"
        (	"ID" NUMBER(19,0),
        "VERSION" NUMBER(10,0),
        "STARTTIME" VARCHAR2(8 CHAR),
        "ENDTIME" VARCHAR2(8 CHAR),
        "CORESERVERFIELD_" NUMBER(19,0)
        )   TABLESPACE

        SEGMENT CREATION IMMEDIATE
        PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
        NOCOMPRESS LOGGING
        STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
        PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
        BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
        TABLESPACE "CMS_TBL" ;

        ========================================================
        CREATE TABLE "CMS"."T_CORESERVER"
        (	"ID" NUMBER(19,0),
        "C_VERSION" NUMBER(10,0),
        "C_URL" VARCHAR2(255 CHAR),
        "C_WEIGHT" NUMBER(10,0),
        "C_RESERVEDURL" VARCHAR2(255 CHAR),
        "C_RESERVEDWEIGHT" NUMBER(10,0),
        "C_ENABLED" NUMBER(1,0),
        "C_USAGETYPE" NUMBER(10,0)
        ) SEGMENT CREATION IMMEDIATE
        PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
        NOCOMPRESS LOGGING
        STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
        PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
        BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
        TABLESPACE "CMS_TBL" ;
        ========================================================
        */
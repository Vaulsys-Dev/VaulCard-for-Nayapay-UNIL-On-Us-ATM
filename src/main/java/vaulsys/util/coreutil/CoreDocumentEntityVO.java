package vaulsys.util.coreutil;

import com.fanap.cms.valueobjects.corecommunication.DocumentResultVO;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.persistence.IEntity;
import org.apache.log4j.Logger;

import javax.persistence.*;

import java.util.List;

@Entity
@Table(name = "core_doc_en")
public class CoreDocumentEntityVO implements IEntity<Long> {
    private transient Logger logger = Logger.getLogger(CoreDocumentEntityVO.class);

    @Id
    @GeneratedValue(generator = "doc-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "doc-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "documentvo_seq")
            })
    private Long id;

    private String machineIP;

    private String barnchId;

    @Column(name = "cmt")
    private String comment;

    private String billNumber;

    @OneToMany(mappedBy = "documentId", fetch = FetchType.LAZY)
    private List<CoreDocumentItemEntityVO> items;

    private String itemComments;
    @Transient
    private String coreRequest;

    @Column(name = "settlement_dt")
    private Long settlementTimeLong;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public List<CoreDocumentItemEntityVO> getItems() {
		return items;
	}

	public void setItems(List<CoreDocumentItemEntityVO> items) {
		this.items = items;
	}

	public String getMachineIP() {
        return machineIP;
    }

    public void setMachineIP(String machineIP) {
        this.machineIP = machineIP;
    }

    public String getBarnchId() {
        return barnchId;
    }

    public void setBarnchId(String barnchId) {
        this.barnchId = barnchId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getItemComments() {
        return itemComments;
    }

    public void setItemComments(String itemComments) {
        this.itemComments = itemComments;
    }

    public String getCoreRequest() {
        return coreRequest;
    }

    public void setCoreRequest(String coreRequest) {
        this.coreRequest = coreRequest;
    }

    public DateTime getSettlementTime() {
        if(settlementTimeLong == null) return null;
        Long dayLong = settlementTimeLong / 1000000L;
        DayDate day = new DayDate();
        day.setDate(dayLong.intValue());

        Long timeLong = settlementTimeLong % 1000000L;
        DayTime time = new DayTime();
        time.setDayTime(timeLong.intValue());

        DateTime dateTime = new DateTime(day, time);
        return dateTime;

//        return settlementTime;
    }

    public void setSettlementTime(DateTime settlementTime) {
        if(settlementTime != null)
            this.settlementTimeLong = settlementTime.getDateTimeLong();
    }

    public static CoreDocumentEntityVO generateVO(DocumentResultVO documentResultVO, DateTime dateTime) {
        CoreDocumentEntityVO coreDocumentEntityVO = new CoreDocumentEntityVO();

        coreDocumentEntityVO.setMachineIP(documentResultVO.getMachineIP());
        coreDocumentEntityVO.setBarnchId(documentResultVO.getBarnchId());
        coreDocumentEntityVO.setComment(documentResultVO.getComment());
        coreDocumentEntityVO.setBillNumber(documentResultVO.getBillNumber());
        coreDocumentEntityVO.setSettlementTime(dateTime);
        coreDocumentEntityVO.setItems(CoreDocumentItemEntityVO.getListFromItem(coreDocumentEntityVO, documentResultVO.getItems(), dateTime));
        coreDocumentEntityVO.setItemComments(documentResultVO.getItemComments());
        coreDocumentEntityVO.setCoreRequest(documentResultVO.getCoreRequest());


        return coreDocumentEntityVO;
    }
}

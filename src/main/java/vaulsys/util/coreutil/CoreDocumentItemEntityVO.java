package vaulsys.util.coreutil;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.persistence.IEntity;
import org.apache.log4j.Logger;
import com.fanap.service.deposit.serviceobjects.DocumentItemVO;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "core_docitem_en")
public class CoreDocumentItemEntityVO implements IEntity<Long> {
    private transient Logger logger = Logger.getLogger(CoreDocumentItemEntityVO.class);

    @Id
    @GeneratedValue(generator = "docItem-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "docItem-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "documentItemvo_seq")
            })
    private Long id;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }


    @Column(name = "settlement_dt")
    private Long settlementTimeLong;
    private String entityType;
    private String identifier;
    private String branchCode;
    private String isDebtor;
    private String amount;
    private String item;

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getIsDebtor() {
        return isDebtor;
    }

    public void setIsDebtor(String isDebtor) {
        this.isDebtor = isDebtor;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documentId", nullable = true, updatable = true)
    @ForeignKey(name="item_doc_fk")
    @Index(name="idx_item_doc")
    private CoreDocumentEntityVO document;

    @Column(name = "documentId", insertable = false, updatable = false)
    private Long documentId;



    public CoreDocumentEntityVO getDocument() {
        return document;
    }

    public void setDocument(CoreDocumentEntityVO document) {
        this.document = document;
    }

    public static List<CoreDocumentItemEntityVO> getListFromItem(CoreDocumentEntityVO coreDocumentEntityVO, List<DocumentItemVO> items, DateTime dateTime) {
        List<CoreDocumentItemEntityVO> result = new ArrayList<CoreDocumentItemEntityVO>();
        for(DocumentItemVO item : items){
            CoreDocumentItemEntityVO coreDocumentItemEntityVO = new CoreDocumentItemEntityVO();

            coreDocumentItemEntityVO.setItem(item.getItemStr());
            coreDocumentItemEntityVO.setEntityType(item.getEntityType());
            coreDocumentItemEntityVO.setIdentifier(item.getIdentifier());
            coreDocumentItemEntityVO.setBranchCode(item.getBranchCode());
            coreDocumentItemEntityVO.setIsDebtor(item.getIsDebtor());
            coreDocumentItemEntityVO.setAmount(item.getAmount());
            coreDocumentItemEntityVO.setSettlementTime(dateTime);


            coreDocumentItemEntityVO.setDocument(coreDocumentEntityVO);
            result.add(coreDocumentItemEntityVO);
        }
        return result;
    }
}

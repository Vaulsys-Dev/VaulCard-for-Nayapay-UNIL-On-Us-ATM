package vaulsys.cms.base;

import vaulsys.persistence.IEntity;

import javax.persistence.*;

/**
 * Created by Mati on 06/08/2020.
 */
@Entity
@Table(name = "CMS_PANFORMAT_FIELDS")
public class CMSPANFormatFields {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "PANFORMATID_FK")
    private CMSPANFormat cmsPanFormat;

    @Column(name = "FIELDNAME")
    private String fieldName;

    @Column(name = "STARTINDEX")
    private String startIndex;

    @Column(name = "LENGTH")
    private String length;

    @Column(name = "VALUE")
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CMSPANFormat getCmsPanFormat() {
        return cmsPanFormat;
    }

    public void setCmsPanFormat(CMSPANFormat cmsPanFormat) {
        this.cmsPanFormat = cmsPanFormat;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(String startIndex) {
        this.startIndex = startIndex;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

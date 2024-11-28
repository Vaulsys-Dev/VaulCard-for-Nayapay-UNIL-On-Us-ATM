package vaulsys.cms.base;

import vaulsys.persistence.IEntity;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Mati on 06/08/2020.
 */
@Entity
@Table(name = "CMS_PANFORMAT")
public class CMSPANFormat {

    @Id
    private Long id;

    @Column(name = "PAN_FORMAT_ID")
    private String panFormatId;

    @Column(name = "FORMAT_TITLE")
    private String formatTitle;

    @Column(name = "PANDESCRIPTION")
    private String panDescription;

    @Column(name = "PANLENGTH")
    private String panLength;

    @Column(name = "ISDELETED")
    private String isDeleted;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "cmsPanFormat")
    private List<CMSPANFormatFields> cmsPanFormatFieldsList;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPanFormatId() {
        return panFormatId;
    }

    public void setPanFormatId(String panFormatId) {
        this.panFormatId = panFormatId;
    }

    public String getFormatTitle() {
        return formatTitle;
    }

    public void setFormatTitle(String formatTitle) {
        this.formatTitle = formatTitle;
    }

    public String getPanDescription() {
        return panDescription;
    }

    public void setPanDescription(String panDescription) {
        this.panDescription = panDescription;
    }

    public String getPanLength() {
        return panLength;
    }

    public void setPanLength(String panLength) {
        this.panLength = panLength;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }

    public List<CMSPANFormatFields> getCmsPanFormatFieldsList() {
        return cmsPanFormatFieldsList;
    }

    public void setCmsPanFormatFieldsList(List<CMSPANFormatFields> cmsPanFormatFieldsList) {
        this.cmsPanFormatFieldsList = cmsPanFormatFieldsList;
    }
}

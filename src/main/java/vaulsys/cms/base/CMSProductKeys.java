package vaulsys.cms.base;

import vaulsys.persistence.IEntity;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * Created by HP on 5/5/2017.
 */
@Entity
@Table(name="cms_productkeys")
public class CMSProductKeys implements IEntity<Long> {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private CMSProduct cmsProductId;

    @Column(name="key_type")
    private String keyType;

    @Column(name="key_value")
    private String keyValue;

    @Column(name="key_check_value")
    private String keyCheckValue;

    @Column(name="key_scheme")
    private String keyScheme;

    @Column(name="pvk_indicator")
    private String pvkIndicator;

    @Column(name="hsm_type")
    private String hsmType;

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long id) {

    }

    public CMSProduct getCmsProductId() {
        return cmsProductId;
    }

    public void setCmsProductId(CMSProduct cmsProductId) {
        this.cmsProductId = cmsProductId;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getKeyCheckValue() {
        return keyCheckValue;
    }

    public void setKeyCheckValue(String keyCheckValue) {
        this.keyCheckValue = keyCheckValue;
    }

    public String getKeyScheme() {
        return keyScheme;
    }

    public void setKeyScheme(String keyScheme) {
        this.keyScheme = keyScheme;
    }

    public String getPvkIndicator() {
        return pvkIndicator;
    }

    public void setPvkIndicator(String pvkIndicator) {
        this.pvkIndicator = pvkIndicator;
    }

    public String getHsmType() {
        return hsmType;
    }

    public void setHsmType(String hsmType) {
        this.hsmType = hsmType;
    }

    public static CMSProductKeys getKeyByType(String keyType, List<CMSProductKeys> keySet) {

        for (int i=0; i<keySet.size(); i++) {
            if (keySet.get(i).getKeyType().equals(keyType))
                return keySet.get(i);
        }
        return null;
    }
}

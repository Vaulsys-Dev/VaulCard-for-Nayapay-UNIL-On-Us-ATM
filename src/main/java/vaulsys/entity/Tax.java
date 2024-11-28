package vaulsys.entity;

import vaulsys.calendar.DateTime;
import vaulsys.contact.Country;
import vaulsys.contact.State;
import vaulsys.persistence.IEntity;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Created by Mati on 30/05/2019.
 */
@Entity
@Table(name = "TAX")
public class Tax implements IEntity<Long> {
    @Id
    private Long id;

    @Column(name = "VALUE")
    private String value;

    @Column(name = "VALUE_TYPE")
    private String valueType;

    @Column(name = "TAX_TYPE")
    private String taxType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STATE")
    @ForeignKey(name = "tax_state_fk")
    private State state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUNTRY")
    @ForeignKey(name = "tax_country_fk")
    private Country country;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "update_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "update_time"))
    })
    private DateTime updateDateTime;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "create_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "create_time"))
    })
    private DateTime createDateTime;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getTaxType() {
        return taxType;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public DateTime getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(DateTime updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public DateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(DateTime createDateTime) {
        this.createDateTime = createDateTime;
    }
}

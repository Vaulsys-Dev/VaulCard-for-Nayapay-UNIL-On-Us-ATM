package vaulsys.config;

import vaulsys.persistence.BaseEntity;

import javax.persistence.*;

/**
 * Created by Asim Shahzad, Date : 27th Aug 2020, Call ID : VC-NAP-202008073/ VC-NAP-202009301
 */
@Entity
@Table(name = "SYSTEM_CONFIG")
@PrimaryKeyJoinColumn(name = "ID")
public class SystemConfig extends BaseEntity<Long> {
    @Id
    private Long id;

    @Column(name = "IDENTIFIER")
    private String identifier;

    @Column(name = "VALUE")
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

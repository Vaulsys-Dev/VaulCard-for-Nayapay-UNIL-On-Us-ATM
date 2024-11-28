package vaulsys.security.base;

import vaulsys.persistence.IEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "secur_parameter")
public class SecurityParameter implements IEntity<Long>{

    @Id
    @GeneratedValue(generator="switch-gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "sec_func")
	@ForeignKey(name="secparam_secfunc_fk")
    private SecurityFunction securityFunction;

    private String name;
    private String value;


    public SecurityParameter() {
    }

    public SecurityParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public SecurityFunction getSecurityFunction() {
        return securityFunction;
    }

    public void setSecurityFunction(SecurityFunction securityFunction) {
        this.securityFunction = securityFunction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

}

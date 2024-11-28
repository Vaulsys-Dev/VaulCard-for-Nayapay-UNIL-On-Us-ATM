 package vaulsys.security.base;

import vaulsys.persistence.IEntity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "secur_function")
public class SecurityFunction implements IEntity<Long>{

    @Id
    @GeneratedValue(generator="switch-gen")
    private Long id;

    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "securityFunction", cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    private Set<SecurityParameter> parameters;

    private String host;
    
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "sec_prof")
	@ForeignKey(name="secfunc_secprof_fk")
    private SecurityProfile securityProfile;

    public SecurityFunction() {

    }

    public SecurityFunction(String name, HashSet<SecurityParameter> parameters) {
        this.name = name;
        this.parameters = (parameters != null) ? parameters : new HashSet<SecurityParameter>();
    }

    public SecurityFunction(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Set<SecurityParameter> getParameters() {
        return parameters;
    }

    public void addParameter(String name, String value) {
        addParameter(new SecurityParameter(name, value));
    }

    public void addParameter(SecurityParameter parameter) {
        if (parameters == null)
            parameters = new HashSet<SecurityParameter>();
        parameter.setSecurityFunction(this);
        parameters.add(parameter);
    }

    public String getParameterValue(String parameterName) {
        for (SecurityParameter parameter : parameters)
            if (parameter.getName().equalsIgnoreCase(parameterName))
                return parameter.getValue();

        return null;
    }

	public SecurityProfile getSecurityProfile() {
		return securityProfile;
	}

	public void setSecurityProfile(SecurityProfile securityProfile) {
		this.securityProfile = securityProfile;
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

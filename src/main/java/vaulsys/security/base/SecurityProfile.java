package vaulsys.security.base;

import vaulsys.calendar.DateTime;
import vaulsys.config.ConfigurationManager;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.configuration.Configuration;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "secur_prof")
public class SecurityProfile implements IEntity<Long> {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "securityProfile", cascade = CascadeType.ALL)
    Set<SecurityFunction> functions;

    @Id
    @GeneratedValue(generator="switch-gen")
    private Long id;

    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "secprof_user_fk")
	protected User creatorUser;

    @AttributeOverrides({
        @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
        @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
            })
	protected DateTime createdDateTime;
    
    public SecurityProfile(String configFile) {
        Configuration config = ConfigurationManager.getInstance().getConfiguration(configFile);

        String[] functions_name = config.getStringArray("Security_Profile/FunctionDefinition/Function/Name");
        functions = new HashSet<SecurityFunction>(functions_name.length);
        for (String name : functions_name) {
            SecurityFunction function = new SecurityFunction(name);
            String[] parameters_name = config.getStringArray("Security_Profile/FunctionDefinition/Function[Name='" + name + "']/Parameters/Parameter/Name");
            for (String pn : parameters_name) {
                String value = config.getString("Security_Profile/FunctionDefinition/Function[Name='" + name + "']/Parameters/Parameter[Name='" + pn + "']/Value");
                function.addParameter(pn, value);
            }

            functions.add(function);
        }
    }

    public SecurityProfile() {
        super();
        functions = new HashSet<SecurityFunction>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SecurityFunction getSecurityFunction(String name) {
        for (SecurityFunction function : functions)
            if (function.getName().equals(name))
                return function;
        return null;
    }

    public Set<SecurityFunction> getFunctions() {
        return functions;
    }

    public void setFunctions(Set<SecurityFunction> functions) {
        this.functions = functions;
    }

    public void addFunction(SecurityFunction securityFunction) {
        if (functions == null)
            functions = new HashSet<SecurityFunction>();
        functions.add(securityFunction);
    }

	@Override
	public String toString() {
		return name;
	}

	public User getCreatorUser() {
		return creatorUser;
	}

	public void setCreatorUser(User creatorUser) {
		this.creatorUser = creatorUser;
	}

	public DateTime getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(DateTime createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
}

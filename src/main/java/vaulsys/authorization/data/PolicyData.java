package vaulsys.authorization.data;

import vaulsys.authorization.policy.Policy;
import vaulsys.persistence.IEntity;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "auth_plc_dt_data")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class PolicyData implements IEntity<Long> {

    @Id
    @GeneratedValue(generator="switch-gen")
    private Long id;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name = "policy")
	@ForeignKey(name="policydata_policy_fk")
    private Policy policy;

    protected PolicyData() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

}

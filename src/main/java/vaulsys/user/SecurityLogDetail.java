package vaulsys.user;

import vaulsys.persistence.IEntity;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;


@Entity
@Table(name = "u_security_detail")
public class SecurityLogDetail  implements IEntity<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6561015510234552830L;

	
	@Id
    @GeneratedValue(generator = "fine-seq-gen2")
    @SequenceGenerator(name = "fine-seq-gen2", allocationSize = 1, sequenceName = "fine_code_seq")
    protected Long code;
	
	
	@Embedded
	@AttributeOverrides({
    @AttributeOverride(name = "action", column = @Column(name = "action"))})
	private UserAction action;
	
	
	 public UserAction getAction() {
		return action;
	}

	public void setAction(UserAction action) {
		this.action = action;
	}

	 @ManyToOne(fetch = FetchType.LAZY)
	 @JoinColumn(name = "securityLog")
	 @ForeignKey(name = "security_log_fk")
	 private SecurityLog securityLog;
	
	 @Column(name = "Field_name")
	 private String FieldName;
	 
	 @Column(name = "Field_type")
	 private Class FeildType;
	 
	 @Column(name = "old_value")
	 private String oldValue;
	 
	 @Column(name = "new_value")
	 private String newValue;
	 
	 @Column(name = "old_value_str",length = 255)
	 private String oldValueStr;
	 
	 @Column(name = "new_value_str",length = 255)
	 private String newValueStr;
	 
	
	 
	 
	
	public SecurityLog getSecurityLog() {
		return securityLog;
	}

	public void setSecurityLog(SecurityLog securityLog) {
		this.securityLog = securityLog;
	}

	public String getFieldName() {
		return FieldName;
	}

	public void setFieldName(String fieldName) {
		FieldName = fieldName;
	}

	public Class getFeildType() {
		return FeildType;
	}

	public void setFeildType(Class feildType) {
		FeildType = feildType;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public String getOldValueStr() {
		return oldValueStr;
	}

	public void setOldValueStr(String oldValueStr) {
		this.oldValueStr = oldValueStr;
	}

	public String getNewValueStr() {
		return newValueStr;
	}

	public void setNewValueStr(String newValueStr) {
		this.newValueStr = newValueStr;
	}

	@Override
	public Long getId() {
		// TODO Auto-generated method stub
		return this.code;
	}

	@Override
	public void setId(Long id) {
		// TODO Auto-generated method stub
		this.code = id;
	}
	

}

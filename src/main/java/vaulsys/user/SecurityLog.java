package vaulsys.user;

import vaulsys.log.Log;

import javax.persistence.*;
import java.util.Set;


@Entity
@Table(name = "u_security")
public class SecurityLog extends Log {
	private String username;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "action", column = @Column(name = "action"))})
	private UserAction action;
	
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "group", column = @Column(name = "group_Sec"))})
	private SecurityGroup group;
	
	@Column(name = "record_id")
	private Long recordID;

	@Column(name = "acc_name")
	private String accessName;
	
	@Column(name = "obj_name")
	private String objectName;

	@Column(name = "acc_desc", length=4000)
	private String accessDesc;

	private boolean authorized;

	@Column(name = "req_ip")
	private String requestIp;
    @OneToMany(mappedBy = "securityLog", fetch = FetchType.LAZY)
	private Set<SecurityLogDetail>  logDetail;

	public SecurityLog() {
	}

	public SecurityLog(String username, UserAction action, String requestIp, boolean authorized, String accessName, String accessDesc) {
		this.username = username;
		this.action = action;
		this.requestIp = requestIp;
		this.authorized = authorized;
		this.accessName = accessName;
		this.accessDesc = accessDesc;
	}
	public Long getRecordID() {
		return recordID;
	}

	public void setRecordID(Long recordID) {
		this.recordID = recordID;
	}

	public SecurityGroup getGroup() {
		return group;
	}

	public void setGroup(SecurityGroup group) {
		this.group = group;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public Set<SecurityLogDetail> getLogDetail() {
		return logDetail;
	}

	public void setLogDetail(Set<SecurityLogDetail> logDetail) {
		this.logDetail = logDetail;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public UserAction getAction() {
		return action;
	}

	public void setAction(UserAction action) {
		this.action = action;
	}

	public String getAccessName() {
		return accessName;
	}

	public void setAccessName(String accessName) {
		this.accessName = accessName;
	}

	public String getAccessDesc() {
		return accessDesc;
	}

	public void setAccessDesc(String accessDesc) {
		this.accessDesc = accessDesc;
	}

	public boolean isAuthorized() {
		return authorized;
	}

	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}

	public String getRequestIp() {
		return requestIp;
	}

	public void setRequestIp(String requestIp) {
		this.requestIp = requestIp;
	}
}

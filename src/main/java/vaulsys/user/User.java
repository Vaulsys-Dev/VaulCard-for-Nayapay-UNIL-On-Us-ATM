package vaulsys.user;

import vaulsys.auditlog.Auditable;
import vaulsys.auditlog.AuditableProperty;
import vaulsys.auditlog.CollectionProperty;
import vaulsys.auditlog.SimpleProperty;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.contact.Contact;
import vaulsys.entity.impl.Branch;
import vaulsys.persistence.BaseEntity;
import org.apache.log4j.Logger;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "u_user")
public class User  extends BaseEntity<Integer> implements Auditable<Integer>{
    private final static Logger logger = Logger.getLogger(User.class);

    @Id
    @GeneratedValue(generator = "switch-gen")
    private Integer id;

    private String password;

    private String passwordSalt;

    private String firstName;

    private String lastName;

    @Column(name = "national_code")
    private String nationalCode;

    @Column(unique = true)
    private String username;

    private String birthPlace;

    @Column(name = "father_name")
    private String fatherName;

    private String prefix;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "gender", column = @Column(name = "u_gender"))})
    private Gender gender;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "status", column = @Column(name = "status"))})
    private UserStatus status = UserStatus.ENABLED;

    @Embedded
    private Contact contact;

    @Column(name = "personnel_code")
    private String personnelCode;

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = Role.class)
    @JoinTable(name = "u_user_roles")
    @ForeignKey(name = "user_roles_fk", inverseName = "user_roleusr_fk")
    private Set<Role> roles;

    private String permissions;

    private String deniedPermissions;

    private String passwordHistory;

    public String getPasswordHistory() {
        return passwordHistory;
    }

    public void setPasswordHistory(String passwordHistory) {
        this.passwordHistory = passwordHistory;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user")
    @ForeignKey(name = "user_user_fk")
    protected User creatorUser;

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = Branch.class)
    @JoinTable(name = "u_user_branches")
    @ForeignKey(name = "user_branches_fk", inverseName = "user_branchusr_fk")
    private Set<Branch> branch;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))})
    protected DateTime createdDateTime = DateTime.UNKNOWN;

    private String lastIp;

    @Transient
    private String previousIp;

    private Boolean logged = false;

    @Column(name = "mnr_exp")
    private Boolean monitorExpirable = true;

    @Column(name = "no_of_tries")
    private Byte noOfWrongTries;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "last_login_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "last_login_time"))})
    private DateTime lastLoginDate = DateTime.UNKNOWN;


    @AttributeOverride(name = "date", column = @Column(name = "last_chg_pass_date"))
    private DayDate lastChgPassDate = DayDate.UNKNOWN;

    @Transient
    private DateTime previousLoginDate;

    @Transient
    private byte[] allAllowedPermissions;

    @Transient
    private byte[] allDeniedPermissions;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "last_unlock_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "last_unlock_time"))})
    private DateTime lastUnLockDate = DateTime.UNKNOWN;

    public User() {
    }

    public User(String username) {
        this.username = username;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DateTime getLastUnLockDate() {
        return lastUnLockDate;
    }

    public void setLastUnLockDate(DateTime lastUnLockDate) {
        this.lastUnLockDate = lastUnLockDate;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return (getPrefix() != null ? getPrefix() + " " : "")
                + (getFirstName() != null ? getFirstName() : "") + " "
                + (getLastName() != null ? getLastName() : "");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public String getPersonnelCode() {
        return personnelCode;
    }

    public void setPersonnelCode(String personnelCode) {
        this.personnelCode = personnelCode;
    }

    public Set<Role> getRoles() {
        if (roles == null)
            roles = new HashSet<Role>();
        return roles;
    }

    public void addRole(Role role) {
        if (this.roles == null) {
            roles = new HashSet<Role>(3);
        }
        this.roles.add(role);
    }

    public void addRoles(Collection<Role> roles) {
        if (this.roles == null) {
            this.roles = new HashSet<Role>(3);
        }
        if (roles != null)
            this.roles.addAll(roles);
    }

    public void addBranches(Collection<Branch> branches) {
        try {
            if (this.branch == null) {
                this.branch = new HashSet<Branch>(3);
            }
            this.branch.clear();
            if (branches != null)
                this.branch.addAll(branches);

        } catch (Exception e) {
            logger.error("Exception in addBranches : Probebly User Has not Branch Access !\r\n" + e);
        }
    }

    public void clearRoles() {
        if (this.roles == null) {
            return;
        }
        roles.clear();
    }

    public void removeRole(Role role) {
        if (roles != null) {
            roles.remove(role);
        }
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public String getDeniedPermissions() {
        return deniedPermissions;
    }

    public void setDeniedPermissions(String deniedPermissions) {
        this.deniedPermissions = deniedPermissions;
    }

    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
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

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public String getPreviousIp() {
        return previousIp;
    }

    public void setPreviousIp(String previousIp) {
        this.previousIp = previousIp;
    }

    public String getNationalCode() {
        return nationalCode;
    }

    public void setNationalCode(String nationalCode) {
        this.nationalCode = nationalCode;
    }

    public Boolean getLogged() {
        return logged;
    }

    public void setLogged(Boolean logged) {
        this.logged = logged;
    }

    public Byte getNoOfWrongTries() {
        return noOfWrongTries;
    }

    public void setNoOfWrongTries(Byte noOfWrongTries) {
        this.noOfWrongTries = noOfWrongTries;
    }

    public DateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(DateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public DateTime getPreviousLoginDate() {
        return previousLoginDate;
    }

    public void setPreviousLoginDate(DateTime previousLoginDate) {
        this.previousLoginDate = previousLoginDate;
    }

    public byte[] getAllAllowedPermissions() {
        return allAllowedPermissions;
    }

    public void setAllAllowedPermissions(byte[] allAllowedPermissions) {
        this.allAllowedPermissions = allAllowedPermissions;
    }

    public byte[] getAllDeniedPermissions() {
        return allDeniedPermissions;
    }

    public void setAllDeniedPermissions(byte[] allDeniedPermissions) {
        this.allDeniedPermissions = allDeniedPermissions;
    }

    public Boolean getMonitorExpirable() {
        return monitorExpirable;
    }

    public void setMonitorExpirable(Boolean monitorExpirable) {
        this.monitorExpirable = monitorExpirable;
    }

    public DayDate getLastChgPassDate() {
        return lastChgPassDate;
    }

    public void setLastChgPassDate(DayDate lastChgPassDate) {
        this.lastChgPassDate = lastChgPassDate;
    }

    public Set<Branch> getBranch() {
        return branch;
    }

    public void setBranch(Set<Branch> branch) {
        this.branch = branch;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }

        User user = (User) o;

        if (!id.equals(user.id)) {
            return false;
        }
        if (username != null ? !username.equals(user.username) : user.username != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = id;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
    	try {
    		return getFirstName() + " " + getLastName() + ", " + getUsername();

		} catch (Exception e) {
			// TODO: handle exception
			return "";
		}
    }
    @Override
    public List<AuditableProperty> getAuditableFields() {
		List<AuditableProperty> props = new ArrayList<AuditableProperty>();
		props.add(new SimpleProperty("firstName"));
		props.add(new SimpleProperty("lastName"));
		props.add(new SimpleProperty("nationalCode"));
		props.add(new SimpleProperty("username"));
		props.add(new SimpleProperty("birthPlace"));
		props.add(new SimpleProperty("fatherName"));
		props.add(new SimpleProperty("prefix"));
		props.add(new SimpleProperty("passwordHistory"));
		props.add(new SimpleProperty("gender.gender"));
		props.add(new SimpleProperty("status.status"));
		props.add(new SimpleProperty("personnelCode"));
		props.add(new SimpleProperty("contact.name"));
		props.add(new SimpleProperty("contact.website.websiteAddress"));
		props.add(new SimpleProperty("contact.website.email"));
		props.add(new SimpleProperty("contact.mobileNumber.areaCode"));
		props.add(new SimpleProperty("contact.mobileNumber.number"));
		props.add(new SimpleProperty("contact.phoneNumber.areaCode"));
		props.add(new SimpleProperty("contact.phoneNumber.number"));
		props.add(new SimpleProperty("contact.address.address"));
		props.add(new SimpleProperty("contact.address.postalCode"));
		props.add(new SimpleProperty("contact.address.country"));
		props.add(new SimpleProperty("contact.address.state"));
		props.add(new SimpleProperty("contact.address.city"));
		props.add(new CollectionProperty("roles"));
		props.add(new SimpleProperty("permissions"));
		props.add(new SimpleProperty("deniedPermissions"));
		props.add(new CollectionProperty("branch"));    	
    	return props;
    }
}

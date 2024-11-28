package vaulsys.util.coreLoadBalancer;

import vaulsys.persistence.IEntity;

import javax.persistence.*;
import java.util.Set;


@Entity
@Table(name = "core_server")
public class CoreServer implements IEntity<Long> {

    @Id
    @GeneratedValue(generator = "switch-gen")
    private Long id;
	public void setId(Long id){
		this.id = id;
	}
	public Long getId(){
		return this.id;
	}

	private Boolean enabled;
	public void setEnabled(Boolean enabled){
		this.enabled = enabled;
	}
	public Boolean getEnabled(){
		return this.enabled;
	}

	private Integer weight;
	public void setWeight(Integer weight){
		this.weight = weight;
	}
	public Integer getWeight(){
		return this.weight;
	}

	private Integer usageType;
	public void setUsageType(Integer usageType){
		this.usageType = usageType;
	}
	public Integer getUsageType(){
		return this.usageType;
	}

	private Integer reservedWeight;
	public void setReservedWeight(Integer reservedWeight){
		this.reservedWeight = reservedWeight;
	}
	public Integer getReservedWeight(){
		return this.reservedWeight;
	}

	private String reservedURL;
	public void setReservedURL(String reservedURL){
		this.reservedURL = reservedURL;
	}
	public String getReservedURL(){
		return this.reservedURL;
	}

	private String url;
	public void setUrl(String url){
		this.url = url;
	}
	public String getUrl(){
		return this.url;
	}

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "coreServer")
    private Set<TimeRange> downTimes;
	public void setDownTimes(java.util.Set downTimes){
		this.downTimes = downTimes;
	}
	public java.util.Set getDownTimes(){
		return this.downTimes;
	}

    public Object clone() throws CloneNotSupportedException {
        return null; //clone.DeepCopy.copy(this); //Raza Commenting clone not found
    }

    @Override
    public String toString() {
        return id.toString();
    }

}
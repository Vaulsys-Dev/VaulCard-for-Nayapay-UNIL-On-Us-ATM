package vaulsys.terminal.atm;

import vaulsys.persistence.IEntity;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "term_atm_rscode_response")
public class RsCodeResponses implements IEntity<Long> {
   
	@Id
    @GeneratedValue(generator="switch-gen")
	private Long id;
	
	private Integer fit;
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "term_atm_rscode__response", 
  		joinColumns = {@JoinColumn(name = "recode_response")},
  		inverseJoinColumns = {@JoinColumn(name = "atm_response")}
  		)
  	@ForeignKey(name = "rscode_resp__rsresp_fk", inverseName = "rscode_resp__atmresp_fk")
	private Map<Integer, ATMResponse> rsCodeResponses;

    public RsCodeResponses() {
    }
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getFit() {
		return fit;
	}

	public void setFit(Integer fit) {
		this.fit = fit;
	}

	public Map<Integer, ATMResponse> getRsCodeResponses() {
		return rsCodeResponses;
	}

	public void setRsCodeResponses(Map<Integer, ATMResponse> rsCodeResponses) {
		this.rsCodeResponses = rsCodeResponses;
	}

	public void addRsCodeResponses(Integer fit, Integer rsCode, ATMResponse response) {
		this.fit = fit;
		if (rsCodeResponses == null)
			rsCodeResponses = new HashMap<Integer, ATMResponse>();
		rsCodeResponses.put(rsCode, response);
	}

	public void addRsCodeResponses(Integer rsCode, ATMResponse response) {
		if (rsCodeResponses == null)
			rsCodeResponses = new HashMap<Integer, ATMResponse>();
		rsCodeResponses.put(rsCode, response);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fit == null) ? 0 : fit.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RsCodeResponses))
			return false;
		RsCodeResponses other = (RsCodeResponses) obj;
		if (fit == null) {
			if (other.fit != null)
				return false;
		} else if (!fit.equals(other.fit))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}

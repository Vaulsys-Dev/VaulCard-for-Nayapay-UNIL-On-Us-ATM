package vaulsys.protocols.ifx.imp;

import vaulsys.util.Util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "IFX_THIRD_PARTY_DATA")
public class ThirdPartyData {
	@Id
	@GeneratedValue(generator="thirdpartydata-seq-gen")
	@org.hibernate.annotations.GenericGenerator(name = "thirdpartydata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
   			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
   			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
   			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "thirdpartydata_seq")
   				})
   	Long id;
	
	@Column(name = "thrd_prt_code")
	private Long ThirdPartyCode;

	@Column( name = "ids")
	private String thirdPartyIds;

	@Transient
	private transient String ThirdPartyName;

	@Transient
	private transient String ThirdPartyNameEn;
	

	public ThirdPartyData() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

	public Long getThirdPartyCode() {
		return ThirdPartyCode;
	}
	
	public void setThirdPartyCode(Long thirdPartyCode) {
		ThirdPartyCode = thirdPartyCode;
	}

	public void copyFields(ThirdPartyData source) {
		if( ThirdPartyCode == null)
			ThirdPartyCode = source.getThirdPartyCode();
		
		if(!Util.hasText(thirdPartyIds))
			thirdPartyIds = source.getThirdPartyIds();
		
	}

	public ThirdPartyData copy() {
		return (ThirdPartyData) clone();
	}

	@Override
	protected Object clone() {
		ThirdPartyData obj = new ThirdPartyData();
		obj.setThirdPartyCode(this.ThirdPartyCode);
		obj.setThirdPartyIds(this.thirdPartyIds);
		return obj;
	}

	public String getThirdPartyName() {
		return ThirdPartyName;
	}

	public void setThirdPartyName(String thirdPartyName) {
		ThirdPartyName = thirdPartyName;
	}

	public String getThirdPartyNameEn() {
		return ThirdPartyNameEn;
	}

	public void setThirdPartyNameEn(String thirdPartyNameEn) {
		ThirdPartyNameEn = thirdPartyNameEn;
	}

	public String getThirdPartyIds() {
		return thirdPartyIds;
	}

	public void setThirdPartyIds(String thirdPartyIds) {
		this.thirdPartyIds = thirdPartyIds;
	}
}

package vaulsys.protocols.ifx.imp;

import java.util.List;

import vaulsys.persistence.IEntity;
import vaulsys.webservices.ghasedak.GhasedakItemType;
import vaulsys.webservices.ghasedak.GhasedakRsItem;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "ifx_ghasedak_data")
public class GhasedakData  implements IEntity<Long> {
	
	@Id
	@GeneratedValue(generator = "ghasedakdata-seq-gen")
	@org.hibernate.annotations.GenericGenerator(name = "ghasedakdata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "ghasedakdata_seq") })
	Long id;

	
//	@OneToMany(mappedBy = "ghasedakData")
//	@Cascade(value = {CascadeType.ALL})
//	private List<GhasedakRsItem> responseItem;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "ghasedakData")
    private List<GhasedakRsItem> ghasedakRsItems;
	
	@Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "itemType", column = @Column(name = "itemtype"))
    })
	private GhasedakItemType itemType;
	
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public GhasedakItemType getItemType() {
		return itemType;
	}

	public void setItemType(GhasedakItemType itemType) {
		this.itemType = itemType;
	}

	public List<GhasedakRsItem> getGhasedakRsItems() {
		return ghasedakRsItems;
	}

	public void setGhasedakRsItems(List<GhasedakRsItem> ghasedakRsItems) {
		this.ghasedakRsItems = ghasedakRsItems;
	}
	
	public void copyFields(GhasedakData source) {
		if(itemType == null)
			itemType = source.getItemType();
		if(ghasedakRsItems == null || ghasedakRsItems.isEmpty())
			ghasedakRsItems = source.getGhasedakRsItems();
	}
	
	public GhasedakData copy() {
		return (GhasedakData) clone();
	}

	@Override
	protected Object clone() {
		GhasedakData obj = new GhasedakData();
		obj.setItemType(this.itemType);
		obj.setGhasedakRsItems(this.ghasedakRsItems);
		return obj;
	}
	
}

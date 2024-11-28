package vaulsys.webservices.ghasedak;

import javax.persistence.*;

import org.hibernate.annotations.ForeignKey;


import java.io.Serializable;


@Entity
@Table(name = "Item")
public class Item implements Serializable {
    @Id
    private long code;

    private String name;

    private String unit;

    private String title;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_data")
	@ForeignKey(name = "item_data_fk")
    ItemData itemData;

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ItemData getItemData() {
        return itemData;
    }

    public void setItemData(ItemData itemData) {
        this.itemData = itemData;
    }
}

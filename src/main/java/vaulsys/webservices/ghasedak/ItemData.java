package vaulsys.webservices.ghasedak;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Item_Data")
public class ItemData implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_data_seq")
    @SequenceGenerator(name = "item_data_seq", sequenceName ="it_dt_seq", allocationSize = 1)
    private long id;

    private String date;

    private Double price;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}

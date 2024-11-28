package vaulsys.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Dell on 2/18/2021.
 */
@Entity
@Table(name = "FBR_ATL")
public class FBRATL {
    @Id
    private Long id;

    @Column(name = "NTN_CNIC")
    private String ntnCnic;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNtnCnic() {
        return ntnCnic;
    }

    public void setNtnCnic(String ntnCnic) {
        this.ntnCnic = ntnCnic;
    }
}

package vaulsys.terminal.impl;


import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "term_pos_grp_files")
public class POSGroupFilesVersion extends POSFilesVersion {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}

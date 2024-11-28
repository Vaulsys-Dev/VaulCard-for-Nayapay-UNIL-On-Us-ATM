package vaulsys.deposit;

import java.io.Serializable;

public class Deposit implements Serializable{

	private Long id;

	private DepositPK depositPK;
	private String tdtitle;
	private Integer state;

	public Deposit() {
	}

	public Deposit(DepositPK depositPK, String tdtitle, Integer state) {
		this.depositPK = depositPK;
		this.tdtitle = tdtitle;
		this.state = state;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTdtitle() {
		return tdtitle;
	}

	public void setTdtitle(String tdtitle) {
		this.tdtitle = tdtitle;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public DepositPK getDepositPK() {

		return depositPK;
	}

	public void setDepositPK(DepositPK depositPK) {
		this.depositPK = depositPK;
	}
}

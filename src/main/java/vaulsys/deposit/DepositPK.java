package vaulsys.deposit;

import java.io.Serializable;

public class DepositPK implements Serializable{

	private Integer abrnchcod;
	private Integer tbdptype;
	private Integer cfcifno;
	private Integer tdserial;

	public DepositPK() {
	}

	public DepositPK(Integer abrnchcod, Integer tbdptype, Integer cfcifno, Integer tdserial) {
		this.abrnchcod = abrnchcod;
		this.tbdptype = tbdptype;
		this.cfcifno = cfcifno;
		this.tdserial = tdserial;
	}

	public Integer getAbrnchcod() {
		return abrnchcod;
	}

	public void setAbrnchcod(Integer abrnchcod) {
		this.abrnchcod = abrnchcod;
	}

	public Integer getCfcifno() {
		return cfcifno;
	}

	public void setCfcifno(Integer cfcifno) {
		this.cfcifno = cfcifno;
	}

	public Integer getTbdptype() {
		return tbdptype;
	}

	public void setTbdptype(Integer tbdptype) {
		this.tbdptype = tbdptype;
	}

	public Integer getTdserial() {
		return tdserial;
	}

	public void setTdserial(Integer tdserial) {
		this.tdserial = tdserial;
	}

	@Override
	public String toString() {
		return String.format("%s-%s-%s-%s", abrnchcod, tbdptype, cfcifno, tdserial);
	}
}

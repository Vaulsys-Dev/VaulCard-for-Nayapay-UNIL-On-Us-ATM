package vaulsys.deposit;

import java.io.Serializable;

public class Card implements Serializable {
	private Integer ciin;
	private Integer crno;
	private Integer cdsqno;
	private Integer cfcifno;

	public Card() {
	}

	public Card(Integer ciin, Integer crno, Integer cdsqno, Integer cfcifno) {
		this.ciin = ciin;
		this.crno = crno;
		this.cdsqno = cdsqno;
		this.cfcifno = cfcifno;
	}

	public Integer getCiin() {
		return ciin;
	}

	public void setCiin(Integer ciin) {
		this.ciin = ciin;
	}

	public Integer getCrno() {
		return crno;
	}

	public void setCrno(Integer crno) {
		this.crno = crno;
	}

	public Integer getCdsqno() {
		return cdsqno;
	}

	public void setCdsqno(Integer cdsqno) {
		this.cdsqno = cdsqno;
	}

	public Integer getCfcifno() {
		return cfcifno;
	}

	public void setCfcifno(Integer cfcifno) {
		this.cfcifno = cfcifno;
	}
}

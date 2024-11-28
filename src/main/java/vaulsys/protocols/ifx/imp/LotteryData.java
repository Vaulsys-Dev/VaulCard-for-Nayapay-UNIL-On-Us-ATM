package vaulsys.protocols.ifx.imp;

import vaulsys.lottery.Lottery;
import vaulsys.lottery.consts.LotteryState;
import vaulsys.persistence.IEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "ifx_lottery_data")
public class LotteryData implements IEntity<Long>, Cloneable {
	
    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="lotterydata-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "lotterydata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "lotterydata_seq")
    				})
    Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="lottery", nullable = true)
	@Cascade(value = { CascadeType.ALL })
	@ForeignKey(name="lotterydata_lottery_fk")
	private Lottery lottery;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "type", column = @Column(name = "lottery_state_prv")) 
	})
	private LotteryState lotteryStatePrv;
	
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "type", column = @Column(name = "lottery_state_nxt")) 
	})
	private LotteryState lotteryStateNxt;
	 
	public LotteryData() {
	}

	
	
	public LotteryData(Lottery lottery, LotteryState prvState, LotteryState nxtState) {
		this.lottery = lottery;
		this.lotteryStatePrv = prvState;
		this.lotteryStateNxt = nxtState;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	protected Object clone() {
		LotteryData obj = new LotteryData();
		obj.setLottery(lottery);
		obj.setLotteryStatePrv(lotteryStatePrv);
		obj.setLotteryStateNxt(lotteryStateNxt);
		return obj;
	}

	public LotteryData copy() {
		return (LotteryData) clone();
	}

	public void copyFields(LotteryData source) {

		if (lottery == null)
			lottery = source.getLottery();

		setLotteryStatePrv(source.getLotteryStatePrv());
		setLotteryStateNxt(source.getLotteryStateNxt());
	}

	public Lottery getLottery() {
		return lottery;
	}

	public void setLottery(Lottery lottery) {
		this.lottery = lottery;
	}

	public LotteryState getLotteryStatePrv() {
		return lotteryStatePrv;
	}

	public void setLotteryStatePrv(LotteryState lotteryStatePrv) {
		this.lotteryStatePrv = lotteryStatePrv;
	}

	public LotteryState getLotteryStateNxt() {
		return lotteryStateNxt;
	}

	public void setLotteryStateNxt(LotteryState lotteryStateNxt) {
		this.lotteryStateNxt = lotteryStateNxt;
	}

}

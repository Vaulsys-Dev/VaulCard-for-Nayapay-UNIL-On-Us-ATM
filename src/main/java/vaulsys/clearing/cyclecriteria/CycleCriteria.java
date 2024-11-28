package vaulsys.clearing.cyclecriteria;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class CycleCriteria implements Serializable {
	@Column(name = "cycletype")
	@Enumerated(EnumType.STRING)
	private CycleType cycleType;
	@Column(name = "cyclecount")
	private Integer cycleCount;
	@Column(name = "houroffset")
	private Integer hourOffset;
	
	//Moosavi: Task 50617 : Add New Policy for max card amount for Currency ATM
	@Column(name = "minuteoffset")
	private Integer minuteOffset;
	//-------------------------------------------------------------------------

	public CycleType getCycleType() {
		return cycleType;
	}

	public void setCycleType(CycleType cycleType) {
		this.cycleType = cycleType;
	}

	public Integer getCycleCount() {
		return cycleCount;
	}

	public void setCycleCount(Integer cycleCount) {
		this.cycleCount = cycleCount;
	}


	//Moosavi: Task 50617 : Add New Policy for max card amount for Currency ATM
	public Integer getHourOffset() {
		return hourOffset;
	}

	public void setHourOffset(Integer hourOffset) {
		this.hourOffset = hourOffset;
	}

	public Integer getMinuteOffset() {
		return minuteOffset;
	}

	public void setMinuteOffset(Integer minuteOffset) {
		this.minuteOffset = minuteOffset;
	}
	//----------------------------------------------------------------------------

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof CycleCriteria)) return false;

		CycleCriteria that = (CycleCriteria) o;

		if (cycleCount != null ? !cycleCount.equals(that.cycleCount) : that.cycleCount != null) return false;
		if (cycleType != null ? !cycleType.equals(that.cycleType) : that.cycleType != null) return false;
		//----------------Moosavi: Task 50617 : Add New Policy for max card amount for Currency ATM------------------
		if (hourOffset != null ? !hourOffset.equals(that.hourOffset) : that.hourOffset != null) return false;
		if (minuteOffset != null ? !minuteOffset.equals(that.minuteOffset) : that.minuteOffset != null) return false;
		//------------------------------------------------------------------------------------------------------------



		return true;
	}

	public int hashCode() {
		int result;
		result = (cycleType != null ? cycleType.hashCode() : 0);
		result = 31 * result + (cycleCount != null ? cycleCount.hashCode() : 0);
		//-Moosavi: Task 50617 : Add New Policy for max card amount for Currency ATM-
		result = 31 * result + (hourOffset != null ? hourOffset.hashCode() : 0);
		result = 31 * result + (minuteOffset != null ? minuteOffset.hashCode() : 0);
		//---------------------------------------------------------------------------
		return result;
	}
	//Moosavi: Task 50617 : Add New Policy for max card amount for Currency ATM
	//Calculate the offset in milisecond to use in converting date time of different tiemzone
	public long calculateOffset()
	{
		return (hourOffset*3600+minuteOffset*60)*1000;
	}
	//---------------------------------------------------------------------------
}

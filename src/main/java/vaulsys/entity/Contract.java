package vaulsys.entity;

import vaulsys.calendar.DayDate;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.hibernate.annotations.Immutable;

@Embeddable
@Immutable
public class Contract implements Serializable {

	private DayDate startDate = DayDate.MIN_DAY_DATE;
    private DayDate endDate = DayDate.MAX_DAY_DATE;

    String contractNumber;
    
    public Contract() {
    }

	public Contract(DayDate startDate) {
		this.startDate = startDate;
	}

    public Contract(DayDate startDate, DayDate endDate, String contractNumber) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.contractNumber = contractNumber;
	}

    public DayDate getStartDate() {
        return startDate;
    }

    public void setStartDate(DayDate startDate) {
        this.startDate = startDate;
    }

    public DayDate getEndDate() {
        return endDate;
    }

    public void setEndDate(DayDate endDate) {
        this.endDate = endDate;
    }

    
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + ((contractNumber == null) ? 0 : contractNumber.hashCode());
		return result;
	}

	public String getContractNumber() {
		return contractNumber;
	}

	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}

}

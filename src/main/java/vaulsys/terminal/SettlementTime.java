package vaulsys.terminal;

import java.io.Serializable;

import vaulsys.calendar.DateTime;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class SettlementTime implements Serializable {

    @Embedded
    @AttributeOverrides({
    @AttributeOverride(name = "dayDate.date", column = @Column(name = "lst_stl_date")),
    @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "lst_stl_time"))
            })
    private DateTime lastSettlementTime;
    @Embedded
    @AttributeOverrides({
    @AttributeOverride(name = "dayDate.date", column = @Column(name = "nxt_stl_date")),
    @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "nxt_stl_time"))
            })
    private DateTime nextSettlementTime;


    public SettlementTime() {
        lastSettlementTime = DateTime.UNKNOWN;
        nextSettlementTime = DateTime.UNKNOWN;
    }

    public SettlementTime(DateTime lastSettlementTime, DateTime nextSettlementTime) {
        this.lastSettlementTime = lastSettlementTime;
        this.nextSettlementTime = nextSettlementTime;
    }

    public DateTime getLastSettlementTime() {
        return lastSettlementTime;
    }

    public void setLastSettlementTime(DateTime lastSettlementTime) {
        this.lastSettlementTime = lastSettlementTime;
    }

    public DateTime getNextSettlementTime() {
        return nextSettlementTime;
    }

    public void setNextSettlementTime(DateTime nextSettlementTime) {
        this.nextSettlementTime = nextSettlementTime;
    }
}

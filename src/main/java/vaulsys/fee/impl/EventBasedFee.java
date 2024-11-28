package vaulsys.fee.impl;

import vaulsys.fee.base.FeeEvent;
import vaulsys.fee.base.FeeInfo;

import java.util.List;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "EventBasedFee")
public class EventBasedFee extends BaseFee{

    final String FeeEvenets = "feeEvenets";

    public EventBasedFee() {
        super();
    }

    public EventBasedFee(String rule, Set<FeeItem> feeItemList) {
        super(rule, feeItemList, true);
    }

    public EventBasedFee(String rule, Set<FeeItem> feeItemList, String description) {
        super(rule, feeItemList, description, true);
    }

    public EventBasedFee(String rule, Set<FeeItem> feeItemList,
                         boolean isEnabled) {
        super(rule, feeItemList, isEnabled);
    }

    public EventBasedFee(String rule, Set<FeeItem> feeItemList, String description,
                         boolean isEnabled) {
        super(rule, feeItemList, description, isEnabled);
    }

    public List<FeeInfo> feeCalculator(List<FeeEvent> feeEvents) {
        return super.feeCalculator(feeEvents);
    }
}

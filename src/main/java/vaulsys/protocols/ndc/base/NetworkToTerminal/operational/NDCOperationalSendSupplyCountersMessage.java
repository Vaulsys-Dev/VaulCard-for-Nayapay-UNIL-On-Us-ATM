package vaulsys.protocols.ndc.base.NetworkToTerminal.operational;

import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCOperationalMsg;
import vaulsys.protocols.ndc.constants.NDCOperationalTypes;

public class NDCOperationalSendSupplyCountersMessage extends NDCOperationalMsg {

    public NDCOperationalSendSupplyCountersMessage() {
        commandCode = NDCOperationalTypes.SEND_SUPPLY_COUNTERS;
    }

    public NDCOperationalSendSupplyCountersMessage(Long logicalUnitNumber) {
        commandCode = NDCOperationalTypes.SEND_SUPPLY_COUNTERS;
        this.logicalUnitNumber = logicalUnitNumber;
    }
}

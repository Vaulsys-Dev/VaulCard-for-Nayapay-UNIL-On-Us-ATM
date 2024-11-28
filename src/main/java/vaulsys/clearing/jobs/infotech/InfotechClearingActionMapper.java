package vaulsys.clearing.jobs.infotech;

import vaulsys.clearing.base.ClearingAction;
import vaulsys.clearing.base.ClearingActionMapper;

public class InfotechClearingActionMapper  implements ClearingActionMapper {
	public static final InfotechClearingActionMapper Instance = new InfotechClearingActionMapper();
	
	private InfotechClearingActionMapper(){}

	@Override
	public ClearingAction findAction(int messageType) {
		return ClearingAction.ACQUIRER_RECONCILEMNET_RESPONSE;
	}

}

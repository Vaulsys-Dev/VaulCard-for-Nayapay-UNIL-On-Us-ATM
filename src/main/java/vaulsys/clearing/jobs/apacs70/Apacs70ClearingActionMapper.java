package vaulsys.clearing.jobs.apacs70;

import vaulsys.clearing.base.ClearingAction;
import vaulsys.clearing.base.ClearingActionMapper;

public class Apacs70ClearingActionMapper implements ClearingActionMapper {
	public static final Apacs70ClearingActionMapper Instance = new Apacs70ClearingActionMapper();
	
	private Apacs70ClearingActionMapper(){}

	@Override
	public ClearingAction findAction(int messageType) {
		return ClearingAction.ACQUIRER_RECONCILEMNET_RESPONSE;
	}

}

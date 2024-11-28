package vaulsys.protocols.fnsPep;

import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.shetab87.Shetab87IFXToISOMapper;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

public class FnsPepIFXToISOMapper extends Shetab87IFXToISOMapper{

	public static final FnsPepIFXToISOMapper Instance = new FnsPepIFXToISOMapper();
	
	private FnsPepIFXToISOMapper(){ super();}
	
	@Override
	public ISOMsg map(Ifx ifx, EncodingConvertor convertor) throws ISOException {
		ISOMsg isoMsg = super.map(ifx, convertor);
		isoMsg.set(32, new Long(639347L).toString());
		
		if (ifx.getReconciliationData() != null){
			isoMsg.set(74, ifx.getReconciliationData().getCreditNumber());
			isoMsg.set(75, ifx.getReconciliationData().getCreditReversalNumber());
			isoMsg.set(76, ifx.getReconciliationData().getDebitNumber());
			isoMsg.set(77, ifx.getReconciliationData().getDebitReversalNumber());
			isoMsg.set(78, ifx.getReconciliationData().getTransferNumber());
			isoMsg.set(79, ifx.getReconciliationData().getTransferReversalNumber());
			isoMsg.set(80, ifx.getReconciliationData().getBallInqNumber());
			isoMsg.set(81, ifx.getReconciliationData().getAuthorizationNumber());
			isoMsg.set(82, ifx.getReconciliationData().getCreditFee());
			isoMsg.set(84, ifx.getReconciliationData().getDebitFee());
			isoMsg.set(86, ifx.getReconciliationData().getCreditAmount());
			isoMsg.set(87, ifx.getReconciliationData().getCreditReversalAmount());
			isoMsg.set(88, ifx.getReconciliationData().getDebitAmount());
			isoMsg.set(89, ifx.getReconciliationData().getDebitReversalAmount());
			long total = ifx.getReconciliationData().getCreditAmount()+ ifx.getReconciliationData().getCreditFee()
						 - ifx.getReconciliationData().getDebitAmount()+ ifx.getReconciliationData().getDebitFee();
			if (total <0)
				total *=-1;
			isoMsg.set(97, total);
//			isoMsg.set(99, GlobalContext.getInstance().getMyInstitution().getBin());
			isoMsg.set(99, ProcessContext.get().getMyInstitution().getBin());
		}
		return isoMsg;
	}
}


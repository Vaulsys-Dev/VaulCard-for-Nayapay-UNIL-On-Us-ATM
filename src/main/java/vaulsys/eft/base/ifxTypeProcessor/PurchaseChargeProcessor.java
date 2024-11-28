package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.OrganizationService;
import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.mtn.ChargeAssignmentPolicy;
import vaulsys.mtn.MTNCharge;
import vaulsys.mtn.MTNChargeService;
import vaulsys.mtn.consts.MTNChargeState;
import vaulsys.mtn.exception.CellChargePurchaseException;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class PurchaseChargeProcessor extends MessageProcessor {

	transient Logger logger = Logger.getLogger(GeneralMessageProcessor.class);

	public static final PurchaseChargeProcessor Instance = new PurchaseChargeProcessor();
	private PurchaseChargeProcessor(){};

	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws Exception {

		Message outgoingMessage = null;
		Ifx outgoingIfx = null;
		MTNCharge charge = null;

		Ifx incomingIfx = incomingMessage.getIfx();
		if (ISOFinalMessageType.isRequestMessage(incomingIfx.getIfxType())) {
			try {
				ChargeAssignmentPolicy ownOrParentChargePolicy = incomingMessage.getEndPointTerminal().getOwnOrParentChargePolicy();
				if (ownOrParentChargePolicy== null){
					ownOrParentChargePolicy = ProcessContext.get().getGeneralChargePolicy();//GlobalContext.getInstance().getGeneralChargePolicy();
				}

				if (incomingIfx.getThirdPartyCode().equals(9912L) &&
						incomingIfx.getAuth_Amt().equals(10000L))
					incomingIfx.setThirdPartyCode(9913L);

					
				charge = ownOrParentChargePolicy.getCharge(incomingIfx);
			} catch (Exception e) {
				logger.error(e.getClass().getSimpleName()+": "+ e.getMessage());
				throw e;
			}
			
			//			if (charge != null) 
			outgoingMessage = createMessage(transaction, incomingMessage, channel, processContext);
			
			outgoingIfx = outgoingMessage.getIfx();
			
			//Mirkamali(Task166): Adapt with Shetab's V7(Send Charge on ALL terminal as billPayment) 
			/*if (TerminalType.ATM.equals(incomingIfx.getTerminalType()) ||
				TerminalType.PINPAD.equals(incomingIfx.getTerminalType()) ||
				TerminalType.VRU.equals(incomingIfx.getTerminalType())) {*/
				outgoingIfx.setIfxType(IfxType.BILL_PMT_RQ);
				outgoingIfx.setTrnType(TrnType.BILLPAYMENT);
				outgoingIfx.setBillOrgType(OrganizationType.UNDEFINED);
				outgoingIfx.setBillID(charge.getCardSerialNo().toString());
				outgoingIfx.setBillPaymentID(charge.getEntity().getCompanyCode().toString() + incomingIfx.getAuth_Amt().toString());
			/*} else {
				outgoingIfx.setIfxType(IfxType.PURCHASE_RQ);
				outgoingIfx.setTrnType(TrnType.PURCHASE);
			}*/
			
			MTNChargeState oldState = charge.getState();
			charge.setState(MTNChargeState.IN_ASSIGNED);

			outgoingIfx.setCharge(charge);
//			outgoingIfx.setCardPIN(charge.getCardPIN());
			outgoingIfx.setChargeStatePrv(oldState);
			outgoingIfx.setChargeStateNxt(charge.getState());
			outgoingIfx.setChargeCompanyCode(charge.getEntity().getCompanyCode());
			outgoingIfx.getCharge().setLifeCycle(transaction.getLifeCycle());
			
			ThirdPartyVirtualTerminal thPVT = OrganizationService.findThirdPartyVirtualTerminalByOrganization(charge.getEntity());
			outgoingIfx.setThirdPartyTerminalId(thPVT.getCode());
			incomingIfx.setThirdPartyTerminalId(thPVT.getCode());
			
			incomingIfx.setChargeData(outgoingIfx.getChargeData());
			GeneralDao.Instance.saveOrUpdate(incomingIfx);
			
	        transaction.setOutgoingIfx(outgoingIfx);
		} else {
			try {
				outgoingMessage = createMessage(transaction, incomingMessage, channel, processContext);
				outgoingIfx = outgoingMessage.getIfx();
				outgoingIfx.setIfxType(IfxType.PURCHASE_CHARGE_RS);
				outgoingIfx.setTrnType(TrnType.PURCHASECHARGE);
				transaction.setDebugTag(outgoingIfx.getIfxType().toString());
				if (transaction.getLifeCycle().equals(outgoingIfx.getCharge().getLifeCycle())) {
					if (ISOResponseCodes.isSuccess(outgoingIfx.getRsCode())) {
						outgoingIfx.getCharge().setState(MTNChargeState.ASSIGNED);
						outgoingIfx.setChargeStatePrv(outgoingIfx.getChargeStateNxt());
						outgoingIfx.setChargeStateNxt(MTNChargeState.ASSIGNED);
						ChargeAssignmentPolicy ownOrParentChargePolicy = outgoingMessage.getEndPointTerminal().getOwnOrParentChargePolicy();
						if (ownOrParentChargePolicy == null)
							ownOrParentChargePolicy = GlobalContext.getInstance().getGeneralChargePolicy();
						
						ownOrParentChargePolicy.update(outgoingIfx);
					} else {
						MTNChargeService.unlockCharge(outgoingIfx, transaction);				
					}
				}
			} catch (Exception e) {
				transaction.setDebugTag(IfxType.PURCHASE_CHARGE_RS.toString());
				logger.error("CellChargePurchaseException! "+ e.getClass().getSimpleName()+": "+ e.getMessage());
				throw new CellChargePurchaseException(e);
			}
		}

		GeneralDao.Instance.saveOrUpdate(outgoingIfx.getCharge());
		GeneralDao.Instance.saveOrUpdate(outgoingIfx);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
		GeneralDao.Instance.saveOrUpdate(transaction);

		return outgoingMessage;
	}

	private Message createMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws CloneNotSupportedException {
		Message outgoingMessage = new Message(MessageType.OUTGOING);
		outgoingMessage.setTransaction(transaction);

		transaction.addOutputMessage(outgoingMessage);
		outgoingMessage.setChannel(channel);

		logger.debug("Process purchase_charge incoming message ");
		Ifx outgoingIfx = MsgProcessor.processor(incomingMessage.getIfx());

		outgoingIfx.setFwdBankId((channel.getInstitutionId() == null ? null : (FinancialEntityService.findEntity(
				Institution.class, channel.getInstitutionId())).getBin().toString()));

		outgoingMessage.setIfx(outgoingIfx);

		setMessageFlag(outgoingMessage, incomingMessage.getRequest(), incomingMessage.getNeedResponse(),
				incomingMessage.getNeedToBeSent(), incomingMessage.getNeedToBeInstantlyReversed());

		Terminal endpointTerminal = getEndpointTerminal(outgoingMessage, incomingMessage.getChannel().getEndPointType(), true, processContext);
		outgoingMessage.setEndPointTerminal(endpointTerminal);
		addNecessaryDataToIfx(outgoingIfx, channel, endpointTerminal);
		return outgoingMessage;
	}

	@Override
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
	}

}

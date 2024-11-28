package vaulsys.netmgmt.component;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.settlement.ATMSupervisorEntryThread;
import vaulsys.message.Message;
import vaulsys.network.NetworkManager;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCConsumerRequestMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCSolicitedStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCTerminalToNetworkMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusEncryptorInitialisationDataMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedCashHandlerStatusMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.ATMConnectionStatus;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.action.ActionInitializer;
import vaulsys.terminal.atm.action.isolated.IsolatedState;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.text.ParseException;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.hibernate.LockMode;

public class NDCNetworkManagementComponent {
	private static final Logger logger = Logger.getLogger(NDCNetworkManagementComponent.class);

	private NDCNetworkManagementComponent() {}
	
    public static NetworkManagementAction processNDCNetworkManagementMessage(ProcessContext processContext, Message inputMessage) throws Exception {
        NDCMsg protocolMessage = (NDCMsg) inputMessage.getProtocolMessage();
        
        if (protocolMessage instanceof NDCUnsolicitedStatusMsg 
        		|| protocolMessage instanceof NDCSolicitedStatusMsg
                || protocolMessage instanceof NDCConsumerRequestMsg) {

        	
            NDCTerminalToNetworkMsg ndcMsg = (NDCTerminalToNetworkMsg) protocolMessage;

			//m.rehman: testing only
			//Long terminalCode = 248700L;
            Long terminalCode = ndcMsg.getLogicalUnitNumber();
            ATMTerminal endpointTerminal = (ATMTerminal) TerminalService.findEndpointTerminalForMessageWithoutIFX(inputMessage, terminalCode);
            
            if (endpointTerminal == null){
            	throw new AuthorizationException("Invalid ATM: terminalCode: "+ terminalCode);
            }
            
            ATMTerminal terminal = endpointTerminal;
//            terminal = (ATMTerminal) GeneralDao.Instance.synchObject(terminal);
            TerminalService.lockTerminal(terminalCode.toString(), LockMode.UPGRADE);
                       

            inputMessage.setEndPointTerminal(terminal);
            
            IoSession session = NetworkManager.getInstance().getResponseOnSameSocketConnectionById(inputMessage.getId());
            
            if (!Util.hasText(terminal.getIP())){
            	String remoteAddress = session.getRemoteAddress().toString();
            	if (Util.hasText(remoteAddress)) {
					terminal.setIP(remoteAddress.substring(1, remoteAddress.indexOf(":")));
					GeneralDao.Instance.saveOrUpdate(terminal);
				}
			}
			if (terminal.getKeySet()== null || terminal.getKeySet().isEmpty())
				ATMTerminalService.addDefaultKeySetForTerminal(terminal);
            
            
//            if (terminal == null) {
//            	logger.error("Not found any endpoint terminal with code: " + terminalCode);
//            	return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
//            }
            
    		try {
//    			IoSession session = NetworkManager.getInstance().getResponseOnSameSocketConnectionById(
//						inputMessage.getId());
				NetworkManager.getInstance().addTerminalOpenConnection(terminal.getIP(), session);
				if (!ATMConnectionStatus.CONNECTED.equals(terminal.getConnection())){
					terminal.setConnection(ATMConnectionStatus.CONNECTED);
					GeneralDao.Instance.saveOrUpdate(terminal);
				}
    		} catch (Exception e) {
    			logger.info("Session of ATM[" + terminal.getCode() + "] cannot be saved!");
    		}

            Transaction lastTransaction = terminal.getLastRealTransaction();
//            Transaction lastTransaction = terminal.getLastTransaction();
			inputMessage.getTransaction().setDebugTag("ATM_CONFIRM"+  ((lastTransaction==null)?"": "_"+lastTransaction.getDebugTag()));

			if (!(protocolMessage instanceof NDCUnsolicitedStatusMsg))
				inputMessage.getTransaction().setReferenceTransaction(lastTransaction);
			
			else if (protocolMessage instanceof NDCUnsolicitedCashHandlerStatusMsg){
				inputMessage.getTransaction().setReferenceTransaction(lastTransaction);				
			}
			
            /********** Create IFX for NDC Network Message **********/
            Transaction firstTransaction = null;
//            if (lastTransaction != null && ShetabFinalMessageType.isWithdrawalOrPartialMessage(lastTransaction.getInputMessage().getIfx().getIfxType()))
//            	firstTransaction = lastTransaction;
//            else
            	firstTransaction = (lastTransaction== null) ? null : lastTransaction.getFirstTransaction();
			
            Ifx inncommingIfx = createIncommingIfx(inputMessage, firstTransaction);
            inputMessage.setIfx(inncommingIfx);
            GeneralDao.Instance.saveOrUpdate(inncommingIfx);
            GeneralDao.Instance.saveOrUpdate(inputMessage);
            GeneralDao.Instance.saveOrUpdate(inputMessage.getMsgXml());
            /**********                                    **********/
            AbstractState action = ActionInitializer.findAction(ndcMsg, terminal);
           
            Message outMsg = null;
            if(action != null){
            	if(action instanceof IsolatedState){
	            	outMsg = action.proceed(inputMessage, terminal);            		
            	}else{
	            	action = action.getNextState(inputMessage, terminal);
	            	outMsg = action.proceed(inputMessage, terminal);

			if(inputMessage.getTransaction().getDebugTag().startsWith("ATM_CONFIRM_")) {
	                 	inncommingIfx.setNetworkTrnInfo(null);
	                 	GeneralDao.Instance.saveOrUpdate(inncommingIfx);
	            	 }

	            	if(!(action instanceof IsolatedState)){
		            	terminal.setCurrentAbstractStateClass(action);
	            	}
            	}
            }
            
            NDCMsg  outputMsg = null;
            if (outMsg != null) {
            	outputMsg = (NDCMsg) outMsg.getProtocolMessage();
            	GeneralDao.Instance.saveOrUpdate(outMsg);
                GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
            	GeneralDao.Instance.saveOrUpdate(outMsg.getIfx());
            }
            
            GeneralDao.Instance.saveOrUpdate(terminal);
            GeneralDao.Instance.saveOrUpdate(inputMessage.getTransaction());
            
            /*****************/
            if (ActionInitializer.isConfigurationInfo((NDCMsg) inputMessage.getProtocolMessage())) {
            	ATMSupervisorEntryThread supervisorEntryThread = new ATMSupervisorEntryThread(terminal);
            	Thread settlementThread = new Thread(supervisorEntryThread);
            	settlementThread.start();
            }
            /*****************/

            if (outputMsg == null){
            	processContext.addPendingRequests(outMsg);
			/****************/
			NetworkManager.getInstance().removeResponseOnSameSocketConnectionById(inputMessage.getId());
			logger.info("removing removeResponseOnSameSocketConnectionById: " + inputMessage.getId());
			/****************/
                return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
            }

            if (((InputChannel) inputMessage.getChannel()).getCommunicationMethod().equals(CommunicationMethod.ANOTHER_SOCKET))
                outMsg.setChannel(((InputChannel) inputMessage.getChannel()).getOriginatorChannel());
            else
                outMsg.setChannel(inputMessage.getChannel());

            outMsg.setEndPointTerminal(inputMessage.getEndPointTerminal());

            outMsg.setEndPointTerminal(terminal);

            processContext.getTransaction().addOutputMessage(outMsg);

            return NetworkManagementAction.OUTPUT_MESSAGE_CREATED;
        } else {

			/****************/
			NetworkManager.getInstance().removeResponseOnSameSocketConnectionById(inputMessage.getId());
			logger.info("removing removeResponseOnSameSocketConnectionById: " + inputMessage.getId());
			/****************/

            return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
        }
    }
    
    protected static Ifx createIncommingIfx(Message message, Transaction transaction) throws ParseException {
    	NDCTerminalToNetworkMsg protocolMessage = (NDCTerminalToNetworkMsg) message.getProtocolMessage();
        Ifx ifx = new Ifx();

        if(protocolMessage instanceof NDCSolicitedStatusEncryptorInitialisationDataMsg)
        	ifx.setIfxType(IfxType.MASTER_KEY_CHANGE_RS);
        else
        	ifx.setIfxType(IfxType.ATM_ACKNOWLEDGE);

        ifx.setIfxDirection(IfxDirection.INCOMING);
        
        ifx.setReceivedDt(message.getStartDateTime());
        ifx.setTerminalId(protocolMessage.getLogicalUnitNumber().toString());
        
        Ifx ifxIn = null;
        if (transaction != null && 
        		transaction.getInputMessage() != null && 
        		transaction.getIncomingIfx()/*getInputMessage().getIfx()*/ != null) {
        
	        ifxIn = transaction.getIncomingIfx()/*getInputMessage().getIfx()*/;
			ifx.setSrc_TrnSeqCntr( ISOUtil.zeroUnPad(ifxIn.getSrc_TrnSeqCntr()));
	        ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(ifxIn.getMy_TrnSeqCntr()));
	        
			if (ifxIn.getNetworkRefId()!= null	)
				ifx.setNetworkRefId(ifxIn.getNetworkRefId());
			else
				ifx.setNetworkRefId(ifx.getSrc_TrnSeqCntr());
	        
	        ifx.setSettleDt(ifxIn.getSettleDt());
	        ifx.setBankId(ifxIn.getBankId());
	        ifx.setFwdBankId(ifxIn.getFwdBankId());
	        ifx.setDestBankId(ifxIn.getDestBankId());
	        ifx.setTerminalType(TerminalType.ATM);
	        ifx.setOrgIdNum(ifxIn.getOrgIdNum());
	        ifx.setOrgIdType(ifxIn.getOrgIdType());
	        ifx.setName(ifxIn.getName());
		 ifx.setOrigDt(DateTime.now());
        }
        return ifx;
    }
}

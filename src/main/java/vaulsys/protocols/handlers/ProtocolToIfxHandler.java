package vaulsys.protocols.handlers;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.exception.MandatoryFieldException;
import vaulsys.authorization.exception.PanPrefixServiceNotAllowedException;
import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.message.Message;
import vaulsys.migration.MigrationDataService;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.epay.EpayProtocol;
import vaulsys.protocols.exception.exception.NotApplicableTypeMessageException;
import vaulsys.protocols.exception.exception.NotMappedProtocolToIfxException;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;

public class ProtocolToIfxHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(ProtocolToIfxHandler.class);

    public final static ProtocolToIfxHandler Instance = new ProtocolToIfxHandler();

    private ProtocolToIfxHandler(){
    }

    @Override
    public void execute(ProcessContext processContext) throws Exception {
//        logger.debug("producing IFX message ...");

        Ifx incomingIfx = null;
        Message incomingMessage = null;
        Transaction transaction = processContext.getTransaction();
        EndPointType endPointType = null;
		try {

			transaction.setDebugTag("Default");

			if (processContext.getInputMessage().isIncomingMessage()) {
				incomingMessage = processContext.getInputMessage();
			} else {
				throw new NotApplicableTypeMessageException();
			}

			ProtocolFunctions mapper = incomingMessage.getChannel().getProtocol().getMapper();
//            ((BaseComponent) mapper).setProcessContext(processContext);

//            EncodingConvertor convertor = GlobalContext.getInstance().getConvertor(incomingMessage.getChannel().getEncodingConvertor());
			EncodingConvertor convertor = ProcessContext.get().getConvertor(incomingMessage.getChannel().getEncodingConverter());
			incomingIfx = mapper.toIfx(incomingMessage.getProtocolMessage(), convertor);

			/*** apacs pinpad & apacs pos have same protocol, we don't know terminal type from protocol to ifx ***/
			endPointType = incomingMessage.getChannel().getEndPointType();
			if (EndPointType.isPhisycalDeviceTerminal(endPointType))
				incomingIfx.setTerminalType(EndPointType.getTerminalType(endPointType));


			if (EndPointType.isPhisycalDeviceTerminal(endPointType) ||
					//                  incomingMessage.getChannel().getProtocol() instanceof PSPProtocol ||
					incomingMessage.getChannel().getProtocol() instanceof EpayProtocol) {
				Long lastTrxId = transaction.getId() % 1000000000000L;
				//m.rehman: commenting below
                    /*incomingIfx.setMyNetworkRefId(
//                    		ConfigUtil.getProperty(ConfigUtil.MY_PSP_BIN).trim().substring(6, 9)+
//                    		ConfigUtil.getProperty(ConfigUtil.MY_PSP_PORT).trim() + 
                    		StringFormat.formatNew(12, StringFormat.JUST_RIGHT, lastTrxId, '0'));*/
			}

			mapper.addIncomingNecessaryData(incomingIfx, transaction);


//            if (incomingMessage.getChannel().getInstitution() != null){
//            	Institution fwdBankId = FinancialEntityService.findEntity(Institution.class,incomingMessage.getChannel().getInstitution());
//            	fwdBankBin = fwdBankId.getBin();
//            }


			setIfxProperties(incomingMessage, incomingIfx);

//	       changeFields(incomingIfx);

			if (incomingIfx.getIfxType() != null)
				transaction.setDebugTag(incomingIfx.getIfxType().toString());

			if (incomingIfx.getStatusDesc() != null && !incomingIfx.getStatusDesc().equals("") && Severity.ERROR.equals(incomingIfx.getSeverity())) {
				throw new NotMappedProtocolToIfxException(incomingIfx.getStatusDesc());
			}

			incomingMessage.setNeedToBeSent(true);
			boolean needTimeOutTrigger = true;
			if (ISOFinalMessageType.isMessageNotToBeReverse(incomingIfx.getIfxType())
					|| ISOFinalMessageType.isResponseMessage(incomingIfx.getIfxType()))
				needTimeOutTrigger = false;

			incomingMessage.setNeedResponse(needTimeOutTrigger);

			Boolean needToBeInstantlyReversed = true;
			if (ISOFinalMessageType.isReversalOrRepeatMessage(incomingIfx.getIfxType())
					|| (ISOFinalMessageType.isResponseMessage(incomingIfx.getIfxType()) && !ISOResponseCodes.isSuccess(incomingIfx.getRsCode()))
					|| !ISOFinalMessageType.isMessageToBeSent(incomingIfx.getIfxType())
					)
				needToBeInstantlyReversed = false;
//            if (incomingIfx.getIfxType().equals(IfxType.CANCEL) ||
//            		incomingIfx.getIfxType().equals(IfxType.PREPARE_BILL_PMT))
//            	needToBeInstantlyReversed = false;
			incomingMessage.setNeedToBeInstantlyReversed(needToBeInstantlyReversed);

		} catch (Exception ex) {
        	logger.warn(ex,ex);
        	if(!ex.toString().contains("ISOException: Unparsable Original Date") &&
        			!ex.toString().contains("No card-details!") &&
        			!ex.toString().contains("POS has wrong date"))
        		logger.error(ex);
            throw ex;
        } finally {
//        	incomingMessage.setIfx(incomingIfx);
//			GeneralDao.Instance.saveOrUpdate(incomingIfx);

        	incomingMessage.setIfxRelatedData(incomingIfx);
			
			// The messages from UI don't have Terminal
			if (!endPointType.equals(EndPointType.UI_TERMINAL)) {
				Terminal endpointTerminal = null;
				try {

					if(incomingIfx.getEndPointTerminal() != null){
						endpointTerminal = incomingIfx.getEndPointTerminal();
					}else{
						endpointTerminal = TerminalService.findEndpointTerminal(incomingMessage, incomingIfx, endPointType);
						incomingIfx.setEndPointTerminal(endpointTerminal);
					}
					
					if (incomingMessage != null)
						incomingMessage.setEndPointTerminal(endpointTerminal);

					try {
		            	if (ISOFinalMessageType.isRequestMessage(incomingIfx.getIfxType()) ||
		        				ISOFinalMessageType.isPrepareMessage(incomingIfx.getIfxType()) ||
		        				ISOFinalMessageType.isPrepareReversalMessage(incomingIfx.getIfxType())||
		        				ISOFinalMessageType.isTrackingMessage(incomingIfx.getIfxType())) {
		            		
//		            	if (!Util.hasText(incomingIfx.getActualAppPAN()))
		            		if (TrnType.GETACCOUNT.equals(incomingIfx.getSecTrnType()) && 
		            				Util.hasText(incomingIfx.getActualAppPAN()))
		            			;
		            		else
		            			incomingIfx.setActualAppPAN(incomingIfx.getAppPAN());
		            	
//		            	if (!Util.hasText(incomingIfx.getActualSecondAppPan()))
		            		if (TrnType.GETACCOUNT.equals(incomingIfx.getSecTrnType()) && 
		            				Util.hasText(incomingIfx.getActualSecondAppPan()))
		            			;
		            		else
		            			incomingIfx.setActualSecondAppPAN(incomingIfx.getSecondAppPan());
		            	}
		            	
		            	MigrationDataService.setRequiredFields(incomingIfx);
		       	    }/* catch(MandatoryFieldException e) {
			        	incomingMessage.setIfx(incomingIfx);
			            GeneralDao.Instance.saveOrUpdate(incomingIfx);
						throw new MandatoryFieldException("ERROR!!!!!!!!!!!!");
		            }*/
			catch (AuthorizationException e) {
		            	incomingMessage.setIfx(incomingIfx);
			            GeneralDao.Instance.saveOrUpdate(incomingIfx);
		            	throw new PanPrefixServiceNotAllowedException("Failed: Pan not allowed on the requested service: "
	            				+ incomingIfx.getAppPAN() + ", " + incomingIfx.getTrnType().toString());
		            	
		            } 
			 catch(Exception e) {
		            	logger.error("exception in setting migration data, " + e, e);
		            }

					//m.rehman: forward bank id should be same as received, commenting following lines
					/*Long fwdBankBin = incomingMessage.getChannel().getInstitution();
					incomingIfx.setFwdBankId(fwdBankBin);*/
					
		            
		            if (endpointTerminal != null && 
		            		EndPointType.isPhisycalDeviceTerminal(endPointType)) {
		            	long currentTimeMillis = System.currentTimeMillis();
						logger.debug("Try to lock terminal " + endpointTerminal.getCode());
//						endpointTerminal = (Terminal) GeneralDao.Instance.optimizedSynchObject((Terminal) endpointTerminal);
						TerminalService.lockTerminal(endpointTerminal.getCode().toString(), LockMode.UPGRADE);
						logger.debug("terminal locked.... " + endpointTerminal.getCode() + ", " + (System.currentTimeMillis()-currentTimeMillis));
					
					}else if (!incomingIfx.getRequest()
							&& !ISOFinalMessageType.isReversalMessage(incomingIfx.getIfxType())
							&& TerminalType.isPhisycalDeviceTerminal(incomingIfx.getTerminalType())
							&& ProcessContext.get().getMyInstitution().getBin().equals(incomingIfx.getBankId())
							&& FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())){
						logger.debug("Try to lock terminal " + incomingIfx.getTerminalId()+" on receiving reponse message!");
						try {
							long currentTimeMillis = System.currentTimeMillis();
							logger.debug("Try to load to upgrade terminal " + incomingIfx.getTerminalId());
//							GeneralDao.Instance.load(Terminal.class, Long.valueOf(incomingIfx.getTerminalId()), LockMode.UPGRADE);
							
							TerminalService.lockTerminal(incomingIfx.getTerminalId(), LockMode.UPGRADE);
							
//							String queryString = "select code from "+ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)+".term_terminal where code = :code for update ";
//							Map<String, Object> params = new HashMap<String, Object>();
//							params.put("code", incomingIfx.getTerminalId());
//							GeneralDao.Instance.executeSqlQuery(queryString, params);
							logger.debug("terminal loaded... " + incomingIfx.getTerminalId() + ", " + (System.currentTimeMillis()-currentTimeMillis));
							
						} catch (Exception e) {
							logger.error("Encounter an exception to lock terminal", e);
						}
					}
					
//					if (endpointTerminal != null && 
//							EndPointType.ATM_TERMINAL.equals(endPointType)
//					/*!EndPointType.isSwitchTerminal(incomingMessage.getChannel().getEndPointType())*/) {
//						logger.debug("Try to lock terminal " + endpointTerminal.getCode());
//						endpointTerminal = (Terminal) GeneralDao.Instance.optimizedSynchObject((Terminal) endpointTerminal);
//						logger.debug("terminal locked.... " + endpointTerminal.getCode());
//					
//					}else if (!incomingIfx.getRequest()
//							&& TerminalType.ATM.equals(incomingIfx.getTerminalType())
////							&& GlobalContext.getInstance().getMyInstitution().getBin().equals(incomingIfx.getBankId())
////							&& FinancialEntityRole.MY_SELF.equals(GlobalContext.getInstance().getMyInstitution().getRole())
//							&& ProcessContext.get().getMyInstitution().getBin().equals(incomingIfx.getBankId())
//							&& FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())
////							&& !TerminalService.isOriginatorSwitchTerminal(incomingMessage.getTransaction().getFirstTransaction().getInputMessage())
//							){
//						logger.debug("Try to lock atm terminal " + incomingIfx.getTerminalId()+" on receiving reponse message!");
//						ATMTerminal atm = null;
//						try {
////							atm = TerminalService.findTerminal(ATMTerminal.class, Long.valueOf(incomingIfx.getTerminalId()));
////							GeneralDao.Instance.synchObject(atm);
//							
//							atm = GeneralDao.Instance.load(ATMTerminal.class, Long.valueOf(incomingIfx.getTerminalId()), LockMode.UPGRADE);
//							
//							logger.debug("terminal locked.... " + incomingIfx.getTerminalId());
//						} catch (Exception e) {
//							logger.error("Encounter an exception to lock atm terminal", e);
//						}
//					}
//					
//					
//					if (endpointTerminal != null && 
//							EndPointType.POS_TERMINAL.equals(endPointType)) {
//						logger.debug("Try to lock terminal " + endpointTerminal.getCode());
//						endpointTerminal = (Terminal) GeneralDao.Instance.optimizedSynchObject((Terminal) endpointTerminal);
//						logger.debug("terminal locked.... " + endpointTerminal.getCode());
//					
//					}else if (!incomingIfx.getRequest()
//							&& TerminalType.POS.equals(incomingIfx.getTerminalType())
//							&& ProcessContext.get().getMyInstitution().getBin().equals(incomingIfx.getBankId())
//							&& FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())
//							){
//						logger.debug("Try to lock pos terminal " + incomingIfx.getTerminalId()+" on receiving reponse message!");
//						POSTerminal pos = null;
//						try {
//							pos = GeneralDao.Instance.load(POSTerminal.class, Long.valueOf(incomingIfx.getTerminalId()), LockMode.UPGRADE);
//							
//							logger.debug("terminal locked.... " + incomingIfx.getTerminalId());
//						} catch (Exception e) {
//							logger.error("Encounter an exception to lock pos terminal", e);
//						}
//					}
//					
//					
//					
//					if (endpointTerminal != null && 
//							EndPointType.PINPAD_TERMINAL.equals(endPointType)) {
//						logger.debug("Try to lock terminal " + endpointTerminal.getCode());
//						endpointTerminal = (Terminal) GeneralDao.Instance.optimizedSynchObject((Terminal) endpointTerminal);
//						logger.debug("terminal locked.... " + endpointTerminal.getCode());
//					
//					}else if (!incomingIfx.getRequest()
//							&& TerminalType.PINPAD.equals(incomingIfx.getTerminalType())
//							&& ProcessContext.get().getMyInstitution().getBin().equals(incomingIfx.getBankId())
//							&& FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())
//							){
//						logger.debug("Try to lock pinpad terminal " + incomingIfx.getTerminalId()+" on receiving reponse message!");
//						PINPADTerminal pinpad = null;
//						try {
//							pinpad = GeneralDao.Instance.load(PINPADTerminal.class, Long.valueOf(incomingIfx.getTerminalId()), LockMode.UPGRADE);
//							
//							logger.debug("terminal locked.... " + incomingIfx.getTerminalId());
//						} catch (Exception e) {
//							logger.error("Encounter an exception to lock pinpad terminal", e);
//						}
//					}
					
		            
					
					
					/**** Check & set last incoming transaction ****/
		           
		            /**
		             * @author: Pooneh Mousavi
		             */

		            Terminal endPointTerminal = incomingMessage.getEndPointTerminal();
					
					if (endPointTerminal != null && !TerminalType.SWITCH.equals(endPointTerminal.getTerminalType())) {
						
						/*if(endpointTerminal.getLastIncomingTransaction() != null && endpointTerminal.getLastIncomingTransaction().getIncomingIfx() != null
							 && incomingIfx.getSrc_TrnSeqCntr().compareTo(endpointTerminal.getLastIncomingTransaction().getIncomingIfx().getSrc_TrnSeqCntr()) <= 0 ) {
							
								throw new AuthorizationException("POS: " + endpointTerminal.getCode() + ", incoming trnSeqCntr: " +  incomingIfx.getSrc_TrnSeqCntr() + ", last Incoming trnSeqCntr: "
							+ endpointTerminal.getLastIncomingTransaction().getIncomingIfx().getSrc_TrnSeqCntr(), false);
						} else*/
							TerminalService.setLastIncomingTransaction(endPointTerminal, incomingMessage, incomingIfx);
					}
					
					
				} catch (Exception e) {
					if (e instanceof MandatoryFieldException) {
						processContext.getTransaction().getInputMessage().setNeedToBeInstantlyReversed(false);
			        	incomingMessage.setIfx(incomingIfx);
			            GeneralDao.Instance.saveOrUpdate(incomingIfx);
						e.printStackTrace();
						logger.info("Exception: ISOtoIFX:: IFX-ID [" + incomingIfx.getId() + "]"); //Raza LOGGING ENHANCED
						throw e;
					}
					if (e instanceof AuthorizationException) {
						processContext.getTransaction().getInputMessage().setNeedToBeInstantlyReversed(false);
						incomingMessage.setIfx(incomingIfx);
						GeneralDao.Instance.saveOrUpdate(incomingIfx);
						e.printStackTrace();
						logger.info("Exception: ISOtoIFX:: IFX-ID [" + incomingIfx.getId() + "]"); //Raza LOGGING ENHANCED
						throw e;
					}
                    if (e instanceof org.hibernate.exception.LockAcquisitionException) {
                        processContext.getTransaction().getInputMessage().setNeedToBeInstantlyReversed(false);
                        incomingMessage.setIfx(incomingIfx);
                        GeneralDao.Instance.saveOrUpdate(incomingIfx);
						e.printStackTrace();
						logger.info("Exception: ISOtoIFX:: IFX-ID [" + incomingIfx.getId() + "]"); //Raza LOGGING ENHANCED
                        throw e;
                    }
                    logger.error("No endpointTerminal is set for Message:"+ e.getClass().getSimpleName() + ": " + e.getMessage(), e);
				}
			}
			/*if(incomingIfx.getIfxType() != null && !Util.hasText(incomingIfx.getMti())) { //Raza Setting MTI
				System.out.println("ProtocolToIfxHandler:: Going to Set MTI..");
				incomingIfx.setMti(String.valueOf(Ifx.fillMTI(incomingIfx.getIfxType(), incomingIfx.getMti())));
			}*/
	    	incomingMessage.setIfx(incomingIfx);
	        GeneralDao.Instance.saveOrUpdate(incomingIfx);
			logger.info("IFX-ID [" + incomingIfx.getId() + "]"); //Raza LOGGING ENHANCED
		}
    }

     private void setIfxProperties(/*INCOMING*/Message incomingMessage, Ifx incomingIfx) {
        incomingIfx.setIfxDirection(IfxDirection.INCOMING);
        incomingIfx.setReceivedDt(incomingMessage.getStartDateTime());

         if (TerminalType.POS.equals(incomingIfx.getTerminalType())) {
                incomingIfx.setANI(incomingMessage.getANI());
                incomingIfx.setDNIS(incomingMessage.getDNIS());
                incomingIfx.setLRI(incomingMessage.getLRI());
        }
    }

   private void changeFields(Ifx incomingIfx) {
		if (incomingIfx.getBankId() != null && incomingIfx.getBankId().equals(936450L)) {
			if (IfxType.TRANSFER_TO_ACCOUNT_RQ.equals(incomingIfx.getIfxType())) {
				incomingIfx.setIfxType(IfxType.SORUSH_REV_REPEAT_RQ);
				GeneralDao.Instance.saveOrUpdate(incomingIfx);
			}
		}
	}

}


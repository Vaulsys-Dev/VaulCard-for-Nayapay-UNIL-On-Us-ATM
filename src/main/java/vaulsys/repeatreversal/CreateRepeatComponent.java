package vaulsys.repeatreversal;

import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class CreateRepeatComponent {

    transient static Logger logger = Logger.getLogger(CreateRepeatComponent.class);

    private CreateRepeatComponent () {}
    
    public static ISOMsg createRepeatIsoMsg(ISOMsg isoMsg) {
        int mti;
        ISOMsg outISOMsg = null;
        try {
            mti = Integer.parseInt(isoMsg.getMTI());
            outISOMsg = (ISOMsg) isoMsg.clone();

            if (mti == Integer.parseInt(ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87)) {
                outISOMsg.setMTI(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_87);
            } else if (mti == Integer.parseInt(ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_93)) {
                outISOMsg.setMTI(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_93);
            } else if (mti == Integer.parseInt(ISOMessageTypes.ACQUIRER_RECON_REQUEST_87)) {
                outISOMsg.setMTI(ISOMessageTypes.ACQUIRER_RECON_ADVICE_87);
            } else if (mti == Integer.parseInt(ISOMessageTypes.ACQUIRER_RECON_REQUEST_93)) {
                outISOMsg.setMTI(ISOMessageTypes.ACQUIRER_RECON_ADVICE_93);
            }
        } catch (NumberFormatException e) {
            logger.error(e);
        } catch (ISOException e) {
            logger.error(e);
        }
        return outISOMsg;

    }

    public static Ifx createRepeatIfx(Ifx inIfx) throws Exception {

        Ifx outIfx = inIfx.copy();
        IfxType ifxType = outIfx.getIfxType();
//        Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
        Long myBin = ProcessContext.get().getMyInstitution().getBin();
        
        if (IfxType.RESTRICTION_RQ.equals(ifxType)) {//gholami
            outIfx.setIfxType( IfxType.RESTRICTION_REV_REPEAT_RQ);
        } 
        if (IfxType.BAL_INQ_RQ.equals(ifxType)) {
            outIfx.setIfxType( IfxType.BAL_REV_REPEAT_RQ);

        } else if (IfxType.PURCHASE_RQ.equals(ifxType)) {
        	outIfx.setIfxType( IfxType.PURCHASE_REV_REPEAT_RQ);
        
        } else if (IfxType.WITHDRAWAL_RQ.equals(ifxType)) {
        	outIfx.setIfxType( IfxType.WITHDRAWAL_REV_REPEAT_RQ);

        }else if (IfxType.WITHDRAWAL_CUR_RQ.equals(ifxType)) {	//Mirkamali(Task179)
        	outIfx.setIfxType( IfxType.WITHDRAWAL_CUR_REV_REPEAT_RQ);

        } else if (IfxType.BILL_PMT_RQ.equals(ifxType)) {
            outIfx.setIfxType( IfxType.BILL_PMT_REV_REPEAT_RQ);

//        } else if (IfxType.BAL_REV_RQ.equals(ifxType)) {
//            outIfx.setIfxType( IfxType.BAL_REV_REPEAT_RQ);

//        } else if (IfxType.BILL_PMT_REV_RQ.equals(ifxType)) {
//            outIfx.setIfxType( IfxType.BILL_PMT_REV_REPEAT_RQ);

        } else if (IfxType.BAL_REV_REPEAT_RQ.equals(ifxType)) {
          outIfx.setIfxType( IfxType.BAL_REV_REPEAT_RQ);
            
        } else if (IfxType.ACQUIRER_REC_RQ.equals(ifxType)) {
            outIfx.setIfxType( IfxType.ACQUIRER_REC_REPEAT_RQ);

        } else if (IfxType.CARD_ISSUER_REC_RQ.equals(ifxType)) {
            outIfx.setIfxType( IfxType.CARD_ISSUER_REC_REPEAT_RQ);

        } else if (IfxType.NETWORK_MGR_RQ.equals(ifxType)) {
            outIfx.setIfxType( IfxType.NETWORK_MGR_REPEAT_RQ);

        } else if (IfxType.RECONCILIATION_RQ.equals(ifxType)) {
            outIfx.setIfxType( IfxType.RECONCILIATION_REPEAT_RQ);

//        } else if (IfxType.TRANSFER_REV_RQ.equals(ifxType)) {
//            outIfx.setIfxType( IfxType.TRANSFER_REV_REPEAT_RQ);
        } else if (ISOFinalMessageType.isForwardingTransferRq(inIfx.getDestBankId(), inIfx.getRecvBankId(), ifxType, myBin, inIfx)) {
        	outIfx.setIfxType(IfxType.TRANSFER_REV_REPEAT_RQ);
        	
        } else if (IfxType.TRANSFER_RQ.equals(ifxType)){
//        		ShetabFinalMessageType.isForwardingTransferRq(inIfx.getDestBankId(), inIfx.getRecvBankId(), ifxType, myBin)){
//        		IfxType.TRANSFER_RQ.equals(ifxType) && (inIfx.getDestBankId().equals(myBin) ^ inIfx.getRecvBankId().equals(myBin))) {
        	outIfx.setIfxType(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ);
        	
/*        } else if (IfxType.TRANSFER_FROM_ACCOUNT_REV_RQ.equals(ifxType)) {
            outIfx.setIfxType( IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ);

        } else if (IfxType.TRANSFER_TO_ACCOUNT_REV_RQ.equals(ifxType)) {
            outIfx.setIfxType( IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ);
        }
*/   	} else if (IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ.equals(ifxType)) {
    		outIfx.setIfxType( IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ);
    	
		} else if (IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ.equals(ifxType)) {
			outIfx.setIfxType( IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ);
			
		} else if (IfxType.BANK_STATEMENT_RQ.equals(ifxType)) {
			outIfx.setIfxType( IfxType.BANK_STATEMENT_REV_REPEAT_RQ);
			
		} else if (IfxType.BANK_STATEMENT_REV_REPEAT_RQ.equals(ifxType)) {
			outIfx.setIfxType( IfxType.BANK_STATEMENT_REV_REPEAT_RQ);
		}		
        
        

        return outIfx;
    }
}

package vaulsys.authorization.component;

import vaulsys.authorization.exception.FITControlNotAllowedException;
import vaulsys.authorization.exception.MandatoryFieldException;
import vaulsys.authorization.exception.NotPaperReceiptException;
import vaulsys.authorization.exception.NotRoundAmountException;
import vaulsys.authorization.exception.NotSubsidiaryAccountException;
import vaulsys.authorization.exception.PanPrefixServiceNotAllowedException;
import vaulsys.authorization.exception.ServiceTypeNotAllowedException;
import vaulsys.authorization.exception.TransactionAmountNotAcceptableException;
import vaulsys.authorization.exception.card.CardAuthorizerException;
import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.billpayment.exception.DuplicateBillPaymentMessageException;
import vaulsys.billpayment.exception.NotValidBillPaymentMessageException;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class AuthorizationHandler extends BaseHandler {
	private static Logger logger = Logger.getLogger(AuthorizationHandler.class);

	public static final AuthorizationHandler Instance = new AuthorizationHandler();

	private AuthorizationHandler(){
	}

	@Override
    public void execute(ProcessContext processContext) throws Exception {
        try {
//            AuthorizationComponent auth = new AuthorizationComponent();
//            auth.setProcessContext(processContext);
        	AuthorizationComponent.authorize(processContext);
        } catch (Exception e) {
        	if( 	e instanceof DuplicateBillPaymentMessageException || 
        			e instanceof FITControlNotAllowedException ||
        			e instanceof NotPaperReceiptException ||
        			e instanceof NotRoundAmountException ||
        			e instanceof PanPrefixServiceNotAllowedException ||
        			e instanceof MandatoryFieldException || 
        			e instanceof NotValidBillPaymentMessageException ||
        			e instanceof TransactionAmountNotAcceptableException ||
        			e instanceof NotSubsidiaryAccountException ||
        			e instanceof CardAuthorizerException ||
        			e instanceof TransactionAmountNotAcceptableException ||
        			e instanceof ServiceTypeNotAllowedException){
        		//Just for exceptions that are not so important....
        		logger.warn(e);
        	}else{
        		logger.error(e);
        	}
            processContext.getTransaction().getInputMessage().setNeedToBeInstantlyReversed(false);
            throw e;
        }
        return;
    }
}

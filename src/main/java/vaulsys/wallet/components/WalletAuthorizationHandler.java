package vaulsys.wallet.components;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.cms.components.CardAuthorizationHandler;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.security.hsm.HardwareSecurityModule;
import vaulsys.util.Util;
import vaulsys.wallet.exception.WalletCardValidationException;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

/**
 * Created by HP on 24-Apr-17.
 */
public class WalletAuthorizationHandler extends BaseHandler {

    private static Logger logger = Logger.getLogger(WalletAuthorizationHandler.class);

    public static final WalletAuthorizationHandler Instance = new WalletAuthorizationHandler();

    private WalletAuthorizationHandler()
    {
    }

    @Override
    public void execute(ProcessContext processContext) throws Exception {
        try {
            int iretval;
            Ifx ifx = processContext.getInputMessage().getIfx();
            if (ifx != null) {

                String addData = ifx.getAddDataPrivate();

                if (Util.hasText(addData) && addData.substring(0, 1).equals("W")) {
                    iretval = WalletDBOperations.Instance.ValidateWalletByOtherInfo(ifx);
                } else {
                    iretval = WalletDBOperations.Instance.ValidateWalletByPan(ifx);
                }

                if (iretval > 0) //Validate Card, Account, Customer and Permission
                {
                    logger.info("Wallet Validated Successfully");
                    //Now validate PIN
                    if (ISOMessageTypes.isRequestMessage(ifx.getMti())) {

                        //following operations will be performed after customer validation
                        //if transaction is pin change, perform pin change and return
                        if (ifx.getIfxType().equals(IfxType.CHANGE_PIN_BLOCK_RQ)) {
                            HardwareSecurityModule.getInstance().PINChange(processContext);
                            return;
                        }

                        //setting auth flags
                        CardAuthorizationHandler.Instance.SetAuthorizationFlags(processContext);

                        //validating card information from HSM, if required
                        if (ifx.getCardAuthFlags().getAuthRequiredFlag()) {

                           HardwareSecurityModule.getInstance().ValidateOnUsCardInfo(processContext);

                        } else {
                            logger.info("No HSM Authorization is required, moving forward");
                        }

                        //validating transaction limit
                        //m.rehman: separating BI from financial incase of limit
                        if (ISOFinalMessageType.isFinancialMessage(ifx.getIfxType(),true)) {
                            logger.info("Deducting Limit for Financial Transaction");
                            WalletDBOperations.Instance.CheckLimit(processContext, Boolean.FALSE);
                        }

                        //deducting wallet amount
                        logger.info("Deducting Amount for Financial Transaction");
                        WalletDBOperations.Instance.CheckWalletAmount(processContext, Boolean.FALSE);

                    } else if (ISOMessageTypes.isReversalRqMessage(ifx.getIfxType())) {
                        //if financial transaction, reverse the limit
                        //m.rehman: separating BI from financial incase of limit
                        if (ISOFinalMessageType.isFinancialMessage(ifx.getIfxType(),true)) {
                            logger.info("Reversing Limit for Reversal Financial Transaction");
                            WalletDBOperations.Instance.CheckLimit(processContext, Boolean.TRUE);

                            //reversing wallet amount
                            logger.info("Reversing Amount for Financial Transaction");
                            WalletDBOperations.Instance.CheckWalletAmount(processContext, Boolean.TRUE);
                        }
                    }
                } else if (iretval == 0) {
                    //Off-Us transaction flow
                    logger.info("OFFUS Wallet Card not allowed. Error!!!");
                    ifx.setRsCode(ISOResponseCodes.TRANSACTION_TIMEOUT); //Raza verify this return code
                    throw new WalletCardValidationException();

                } else {
                    logger.error("Card Validation Failed");
                    ifx.setRsCode(ISOResponseCodes.LIMIT_EXCEEDED); //Raza verify this return code
                    throw new WalletCardValidationException();
                }

                //setting output channel
                if(ifx.getRsCode() == null || ifx.getRsCode()=="")
                {
                    ifx.setRsCode(ISOResponseCodes.APPROVED);
                }
                ifx.setMti(ISOMessageTypes.getResponseMTI(ifx.getMti())); //setting MTI
                processContext.setOutputChannel(processContext.getInputMessage().getChannel());


            } else {
                logger.error("Ifx not found for Card Validation..!");
                throw new Exception(); //Raza Specify Exception
            }
        } catch (Exception e) {
            e.printStackTrace();
            processContext.getTransaction().getInputMessage().setNeedToBeInstantlyReversed(false);
            throw e;
        }
        return;
    }
}

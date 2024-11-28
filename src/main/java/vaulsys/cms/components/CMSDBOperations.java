package vaulsys.cms.components;


import vaulsys.authorization.exception.card.CardNotFoundException;
import vaulsys.authorization.exception.onlineBillPayment.ExpireDateException;
import vaulsys.cms.base.CMSAccount;
import vaulsys.cms.base.CMSCardRelation;
import vaulsys.cms.exception.AccountNotFoundException;
import vaulsys.cms.exception.CustomerNotFoundException;
import vaulsys.cms.exception.TransactionNotAllowedException;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.config.IMDType;
import vaulsys.entity.impl.IMD;
import vaulsys.util.Util;
import org.apache.log4j.Logger;
import javax.swing.text.DateFormatter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by HP on 24-Apr-17.
 */
public class CMSDBOperations {

	private static Logger logger = Logger.getLogger(CMSDBOperations.class);

    public static final CMSDBOperations Instance = new CMSDBOperations();
	
	private CMSDBOperations()
    {}

    public static int ValidateCardbyPan(Ifx ifx) //Note: 0:OFFUS 1:ONUS-Success -1:Failure
    {
        try {
             if(!ValidateExpiry(ifx)) //Check Expiry
             {
                 logger.info("Expired Card");
                 throw new ExpireDateException();
             }

            String CardRelId = ifx.getTrk2EquivData();

            if (CardRelId != null) {
                int index = CardRelId.indexOf('=');
                if (index == -1) {
                    index = CardRelId.indexOf('D');
                    CardRelId = CardRelId.replace('D', '=');
                }

                CardRelId = CardRelId.substring(0, index) + CardRelId.substring(index, index + 5);
            }
            else
            {
                CardRelId = ifx.getAppPAN() + "=" + ifx.getExpDt(); //Make Card_Relation_ID with PAN & Expiry
            }
            //Boolean isLocalFlag;
            //isLocalFlag = CMSDBOperations.Instance.getIMDType(CardRelId.substring(0,11));

            String Channelid = ifx.getTransaction().getInputMessage().getChannel().getChannelId();

            //For Local Card
            IMDType imdType = CMSDBOperations.Instance.getIMDType(CardRelId.substring(0, 11));
            if (imdType != null && imdType.equals(IMDType.Local)) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("cardrelationid", CardRelId);
                param.put("isdefault", "1");
                param.put("channel", Channelid);
                String query = "from " + CMSCardRelation.class.getName() + " cr " +
                        " where "
                        + " cr.card_relid = :cardrelationid "
                        + "and cr.channel = :channel "
                        + "and cr.isdefault = :isdefault";


                //try {
                CMSCardRelation cmsCardRelation = (CMSCardRelation) GeneralDao.Instance.findObject(query, param);

                if (cmsCardRelation == null) {
                    throw new CardNotFoundException();
                } else if (cmsCardRelation.getAccount() == null) {
                    throw new AccountNotFoundException();
                } else if (cmsCardRelation.getCustomer() == null) {
                    throw new CustomerNotFoundException();
                } else {
                    //CMSStatusCodes.LoadCodes(); //Raza only for Testing, move to Global Context
                    //Check Card Status
                    String cstatus = cmsCardRelation.getCardAuth().getStatus();
                    if (!cstatus.equals("00")) //palce it in like ISOResponseCodes
                    {
                        logger.info("Card found with Negative Status [" + CMSStatusCodes.CardStausMap.get(cstatus) + "]");
                        throw new CardNotFoundException();
                    } else {
                        logger.info("Card found with Status [" + CMSStatusCodes.CardStausMap.get(cstatus) + "]"); //Raza TEMP
                        String astatus = cmsCardRelation.getAccount().getStatus();
                        if (!astatus.equals("00")) //Check Account Status
                        {
                            logger.info("Card found with Negative Status [" + CMSStatusCodes.AcctStausMap.get(astatus) + "]");
                            throw new AccountNotFoundException();
                        } else {
                            logger.info("Account found with Status [" + CMSStatusCodes.AcctStausMap.get(astatus) + "]"); //Raza TEMP
                            String csstatus = cmsCardRelation.getCustomer().getStatus();

                            if (!csstatus.equals("00")) //Check Customer Status
                            {
                                logger.info("Customer found with Negative Status [" + CMSStatusCodes.CustStausMap.get(csstatus) + "]");
                                throw new CustomerNotFoundException();
                            } else {
                                logger.info("Customer found with Status [" + CMSStatusCodes.CustStausMap.get(csstatus) + "]"); //Raza TEMP
                                logger.info("Customer Validated OK!");
                                ifx.setCmsCardRelation(cmsCardRelation);
                            }
                        }
                    }

                    //System.out.println("Card [" + cmsCardRelation.getCard().getAcctNumber() + "]"); //Raza TEMP
                    //System.out.println("Account [" + cmsCardRelation.getAccount().getAccountNumber() + "]"); //Raza TEMP
                    //System.out.println("Customer [" + cmsCardRelation.getCustomer().getFirstname() + "]"); //Raza TEMP
                    //System.out.println("PIN Offset [" + cmsCardRelation.getCardAuth().getEncryptedPin() + "]");

                    //Check Txn Permission
                    if (cmsCardRelation.getTxn_perm() != null) {
                        //if (cmsCardRelation.getTxn_perm().toLowerCase().contains(ifx.getTrnType().toString().toLowerCase())) {
                        if (cmsCardRelation.getTxn_perm().toLowerCase().contains(""+ifx.getTrnType().getType())) { //Raza updating for Type
                            logger.info("Txn Permission verified..! Access Granted");
                        } else {
                            logger.info("Txn Permission verified..! Txn Not Allowed");
                            throw new TransactionNotAllowedException();
                            //throw TxnNotAllowed Exception
                        }
                    } else {
                        logger.info("No Permission Defined for Card and Channel");
                        throw new TransactionNotAllowedException();
                    }

                    return 1;
                }
                //} catch (Exception e) {
                //    e.printStackTrace();
                //    return false;
                //}

            } else {
                logger.info("Offus Card");
                return 0; //return true for OFFUS card
            }
            //CARDRELATIONID
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return -1;
        }
         //Raza TEMP

        /*String query = "from " + CMSAccount.class.getName() + " c ";
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("card", ifx.getAppPAN());
        CMSAccount cmsacc = (CMSAccount)GeneralDao.Instance.findObject(query, param);*/
    }

    public static IMDType getIMDType (String inAppPanIMD) {
        Map<String, Object> dbParam;
        List<String> imdList;
        List<IMD> imdFromDB;
        Integer imdLength;
        String query;
        //Boolean flag;
        IMDType type;

        dbParam = new HashMap<String, Object>();
        imdList = new ArrayList<String>();
        imdLength = inAppPanIMD.length();
        //flag = Boolean.FALSE;
        type = null;

        for (int i=imdLength; i>=6; i--)
            imdList.add(inAppPanIMD.substring(0,i));

        dbParam.put("imdList", imdList);
        query = "from " + IMD.class.getName() + " i where i.IMD in (:imdList)";
        imdFromDB = GeneralDao.Instance.find(query, dbParam);

        if (imdFromDB.isEmpty()) {
            logger.error("IMD Type: No IMD found, setting IMD Type flag to false");
        } else if (imdFromDB.size() > 1) {
            logger.error("IMD Type: Multiple IMDs found, setting IMD Type flag to false");
        } else {
            /*if (imdFromDB.get(0).getIMD_Type().equals(IMDType.Local)) {
                flag = Boolean.TRUE;
            }*/
            type = imdFromDB.get(0).getIMD_Type();
        }

        return type;
    }

    public static boolean ValidateExpiry(Ifx ifx)
    {
        try {
            //Check Expiry Here.. before fetching..!
            String ExpDate;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMM");
            Date curdate = new Date();
            Calendar cal = Calendar.getInstance();
            //System.out.println("Current Date [" + dateFormat.format(curdate) + "]");
            //System.out.println("Current Date [" + curdate + "]");
            //if (ExpDate != "") {
            if (ifx.getExpDt() != null) {
                //try {
                ExpDate = ifx.getExpDt().toString();
                dateFormat.set2DigitYearStart(cal.getTime());
                //cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                //Date lastDayOfMonth = cal.getTime();
                //dateFormat.setCalendar(cal);
                Date expdate = dateFormat.parse(ExpDate);
                //System.out.println("Expiry Date [" + expdate + "]");
                cal.setTime(expdate);
                cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
                //System.out.println("Expiry Date with last day of week [" + cal.getTime() + "]");
                if (cal.before(curdate)) { //expdate.before(curdate)) {
                    return false;
                }
            }
            //also verify expiry using track2 below
            String CardRelId = ifx.getTrk2EquivData();
            if (CardRelId != null) {
                int index = CardRelId.indexOf('=');
                if (index == -1) {
                    index = CardRelId.indexOf('D');
                    CardRelId = CardRelId.replace('D', '=');
                }

                CardRelId = CardRelId.substring(0, index) + CardRelId.substring(index, index + 5);

                //try { //Raza Note: Check Expiry from Track2 also, inorder to maintain integrity of txn
                ExpDate = CardRelId.substring(index + 1, index + 5);
                Date expdate = dateFormat.parse(ExpDate);
                dateFormat.set2DigitYearStart(cal.getTime());
                cal.setTime(expdate);
                cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
                //System.out.println("Expiry Date with last day of week [" + cal.getTime() + "]");
                if (cal.before(curdate)) { //expdate.before(curdate)) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true; //case when Track2 not present. verify!
    }
}

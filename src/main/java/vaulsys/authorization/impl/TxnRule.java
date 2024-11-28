package vaulsys.authorization.impl;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.customer.Currency;
import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.security.securekey.SecureKey;
import vaulsys.util.Util;
import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

/**
 * Created by Raza on 23-Feb-17.
 */
@Entity
@Table(name = "txn_rule")
public class TxnRule implements IEntity<Long> {

    @Id
    private Long RuleId;

    private String Rulename;

    private String srcchannel;

    private String Key;

    @Transient
    static Logger logger = Logger.getLogger(TxnRule.class); //For Logging

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rulecondid")
    @Cascade(value = CascadeType.ALL )
    @ForeignKey(name="RuleCondid_fk")
    private TxnRuleCond RuleCond;

    public Long getId() {
        return RuleId;
    }

    public void setId(Long ruleid) {
        this.RuleId = ruleid;
    }














    public static boolean ValidateTxn(Ifx ifx, String srcchannel, String destchannel) throws Exception //(String institutionid, Ifx ifx)
    {
        String keyquery = "from "+ TxnRuleKey.class.getName() + " m "
                + " where m.channel = :inchannel";
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("inchannel", srcchannel);
        List<TxnRuleKey> keylist = GeneralDao.Instance.find(keyquery, param);

        if(keylist != null && keylist.size() > 0) {
            String Key = TxnRuleKey.getKey(keylist, ifx);

            keyquery = "from "+ TxnRule.class.getName() + " m "
                    + " where m.srcchannel = :inchannel"
                    + " and m.Key = :key";
            param = null;
            param = new HashMap<String, Object>();
            param.put("inchannel", srcchannel);
            param.put("key", Key);
            List<TxnRule> rulelist = GeneralDao.Instance.find(keyquery, param);

            if(rulelist != null && rulelist.size() > 0)
            {
                List<TxnRuleCond> RList = new ArrayList<TxnRuleCond>();

                for(TxnRule rule : rulelist)
                {
                    RList.add(rule.getRuleCond());
                }

                int iretval =  TxnRuleCond.ValidateRuleConditions(RList,ifx);
                if(ifx.getRsCode() != ISOResponseCodes.APPROVED)
                {
                    return false;
                }
                if(iretval == 1)
                {
                    logger.info("Rule Applied and Validated OK");
                    return true;
                }
                else if(iretval == 0)
                {
                    logger.info("No Rule Applied");
                    return true;
                }
                else
                {
                    logger.info("Rule Applied and Validation Failed...");
                    return false;
                }
            }
            else
            {
                logger.info("No Rule Found for Key [" + Key + "]");
                return true;
            }


//            if(RList != null && RList.size() > 0)
//            {
//                for (TxnRuleCond tRuleCond : RList) {
//                    System.out.println("**************************************************");
//                    System.out.println("RuleDef ID [" + tRuleCond.getRuleDef().getId() + "]");
//                    System.out.println("Field [" + tRuleCond.getRuleDef().getField() + "]");
//                    System.out.println("Value [" + tRuleCond.getRuleDef().getValue() + "]");
//                    System.out.println("Operator [" + tRuleCond.getRuleDef().getOperator() + "]");
//                    System.out.println("**************************************************");
//
//                    String field = tRuleCond.getRuleDef().getField();
//                    String operator = tRuleCond.getRuleDef().getOperator();
//                    if(operator == null || field == null) //change it null then work in else
//                    {
//                        System.out.println("Rule Not Defined Properly");
//                        return false;
//                    }
//                    else
//                    {
//                        logger.info("Applying Rule... Field [" + field + "] operator [" + operator + "] Req Value [" + tRuleCond.getRuleDef().getValue()  + "]");
//                        //if(operator.equals("=") || operator.equals("=="))
//                        //{
//                        if(field.equals("TRAN_CODE")) //DE-2
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getTrnType().getType(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("AMOUNT_TRAN")) //DE-4
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getAuth_Amt(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("IMD")) //DE-2 substr(0,6)
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getAppPAN().substring(0,6),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("PAN")) //DE-2
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getAppPAN(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("AMOUNT_SETT")) //DE-5
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getSett_Amt(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("AMOUNT_CARD_BILL")) //DE-6
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getSec_Amt(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("DATE_EXP")) //DE-14
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getExpDt(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("DATE_SETT")) //DE-15
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getSettleDt(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("ACQ_INS_CNTRY_CODE")) //DE-19
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getMerchCountryCode(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("POS_ENT_MODE")) //DE-22
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getPosEntryModeCode(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("POS_COND_CODE")) //DE-25
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getPosConditionCode(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("POS_PIN_CAP_CODE")) //DE-26
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getPosPinCaptureCode(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("ACQ_INS_ID")) //DE-32
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getBankId(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("FWD_INS_ID")) //DE-33
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getFwdBankId(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("CARD_ACCPT_TERM_ID")) //DE-41
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getTerminalId(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("CARD_ACCPT_ID_CODE")) //DE-42
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getOrgIdNum(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("CARD_ACCPT_NAME_LOC")) //DE-43
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getCardAcceptNameLoc(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("CURRENCY_TRAN")) //DE-49
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getAuth_Currency(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("CURRENCY_SETT")) //DE-50
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getSett_Currency(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("CURRENCY_C_BILL")) //DE-51
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getSec_Currency(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("POS_DATA_F60")) //DE-60
//                        {
//                            TxnRuleCond.ValidateField(""+ifx.getSelfDefineData(),tRuleCond.getRuleDef().getValue(),operator);
//                        }
//                        else if(field.equals("RECORD_DATA")) //DE-120
//                        {
//                            System.out.println("Implement whis Field accordingly");
//                        }
//                        //}
//                        //else if(operator.equals("!=") || operator.equals("<>"))
//                        //{}
//                        //else if(operator.equals(">"))
//                        //{}
//                        //else if(operator.equals(">="))
//                        //{}
//                        //else if(operator.equals("<"))
//                        //{}
//                        //else if(operator.equals("<="))
//                        //{}
//                        //else if(operator.toLowerCase().contains("substring"))
//                        //{
////                            int index1,index2,index3;
////                            index1 = operator.indexOf('(',0);
////                            index2 = operator.indexOf(',',0);
////                            index3 = operator.indexOf(')',0);
////
////                            String startindex = operator.substring(index1+1,index1+1+(index2-index1-1));
////                            String endindex = operator.substring(index2+1,index2+1+(index3-index2-1));
////
////                            System.out.println("startindex [" + startindex + "]");
////                            System.out.println("endindex [" + endindex + "]");
////                        }
////                        else if(operator.toUpperCase().equals("IS NULL"))
////                        {}
////                        else if(operator.toUpperCase().equals("IS NOT NULL"))
////                        {}
////                        else if(operator.toUpperCase().equals("IN"))
////                        {}
////                        else if(operator.toUpperCase().equals("NOT IN"))
////                        {}
////                        else if(operator.toUpperCase().equals("STARTS WITH"))
////                        {}
////                        else if(operator.toUpperCase().equals("ENDS WITH"))
////                        {}
////                        else if(operator.toUpperCase().equals("CONTAINS"))
////                        {}
////                        else
////                        {
////                            System.out.println("UN-RECOGNIZED OPERATOR VALUE [" + operator + "]"); //Set to Logger
////                        }
//                        //}
//                        //else
//                        //{
//                        //   System.out.println("UN-RECOGNIZED FIELD"); //Set to Logger
//                        //}
//                    }
//                }
//            }
        }
        else
        {
            logger.info("No Rule Found for Channel [" + srcchannel + "]");
            return true;
        }




        //return 0 = No Rule Applied ; 1 = Rule Applied and Authorized ; -1 = Rule Applied and Not Authorized
        //Ifx ifx = new Ifx(); //Raza for Testing
        //ifx.setTrnType(TrnType.BALANCEINQUIRY); //Raza for Testing
        //ifx.setAppPAN("1234567890123=1234"); //Raza for Testing
        /*String query = "SELECT *\n" +
                "FROM TXN_RULE_DEF\n" +
                "INNER JOIN TXN_RULE\n" +
                "ON txn_rule.RULEDEFID=TXN_RULE_DEF.RULEDEFID and txn_rule.trancode = '00' and txn_rule.msgtype = '0100'";

        List<TxnRuleDef> result = null; //= GeneralDao.Instance.find(query);
        result.addAll(GeneralDao.Instance.executeSqlQuery(query));*/

        //Map<Integer, Currency> currencies = new HashMap<Integer, Currency>();

//        //String query = "from "+ TxnRuleCond.class.getName() + " m "
//                //+ " where m.srcchannel = :inchannel";
                //+ " and m.TranType = :tran_type"; //Raza Make TranType condition as 'in' instead of '='

        //logger.info("TranType [" + ifx.getTrnType().toString() + "]");
        //logger.info("TranType [" + ifx.getTrnType().toString() + "]");
        //logger.info("TranType [" + ifx.getTrnType().toString() + "]");
        //System.out.println("TranType [" + ifx.getTrnType().toString() + "]");

//        //HashMap<String, Object> parameters = new HashMap<String, Object>();
//        //parameters.put("inchannel", srcchannel);
        //parameters.put("tran_type", ifx.getTrnType().toString());

//        //List<TxnRule> RuleList = GeneralDao.Instance.find(query, parameters);






        //List<TxnRuleCond> RList = GeneralDao.Instance.find(query, parameters);




        //return false;
    }

//    public static int ValidateField(String inField,String ReqValue,String Operator)
//    {
//        if(Operator.equals("=") || Operator.equals("==")) {
//            if(inField.equals(ReqValue))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.equals("!=") || Operator.equals("<>"))
//        {
//            if(!inField.equals(ReqValue))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.equals(">"))
//        {
//            Long LinField = Long.parseLong(inField);
//            Long LReqValue = Long.parseLong(ReqValue);
//            if(LinField > LReqValue)
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.equals(">="))
//        {
//            Long LinField = Long.parseLong(inField);
//            Long LReqValue = Long.parseLong(ReqValue);
//            if(LinField >= LReqValue)
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.equals("<"))
//        {
//            Long LinField = Long.parseLong(inField);
//            Long LReqValue = Long.parseLong(ReqValue);
//            if(LinField < LReqValue)
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.equals("<="))
//        {
//            Long LinField = Long.parseLong(inField);
//            Long LReqValue = Long.parseLong(ReqValue);
//            if(LinField <= LReqValue)
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.toLowerCase().contains("substring"))
//        {
//            int index1,index2,index3;
//            index1 = Operator.indexOf('(',0);
//            index2 = Operator.indexOf(',',0);
//            index3 = Operator.indexOf(')',0);
//
//            String startindex = Operator.substring(index1+1,index1+1+(index2-index1-1));
//            String endindex = Operator.substring(index2+1,index2+1+(index3-index2-1));
//
//            //System.out.println("startindex [" + startindex + "]");
//            //System.out.println("endindex [" + endindex + "]");
//        }
//        else if(Operator.toUpperCase().equals("IS NULL"))
//        {
//            if(inField == null || inField.equals(""))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.toUpperCase().equals("IS NOT NULL"))
//        {
//            if(inField != null || !inField.equals(""))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.toUpperCase().equals("IN"))
//        {
//            String FrstVal = "(" + inField + ","; //if first element
//            String MidVal = "," + inField + ","; //if Middle element
//            String LstVal = "," + inField + ")"; //if last element
//            String SnglVal = "(" + inField + ")"; //if only element
//
//            if(ReqValue.contains(FrstVal) || ReqValue.contains(MidVal) || ReqValue.contains(LstVal) || ReqValue.contains(SnglVal))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.toUpperCase().equals("NOT IN"))
//        {
//            String FrstVal = "(" + inField + ","; //if first element
//            String MidVal = "," + inField + ","; //if Middle element
//            String LstVal = "," + inField + ")"; //if last element
//            String SnglVal = "(" + inField + ")"; //if only element
//
//            if(ReqValue.contains(FrstVal) || ReqValue.contains(MidVal) || ReqValue.contains(LstVal) || ReqValue.contains(SnglVal))
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//            else
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//        }
//        else if(Operator.toUpperCase().equals("STARTS WITH"))
//        {
//            if(inField.startsWith(ReqValue))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.toUpperCase().equals("ENDS WITH"))
//        {
//            if(inField.endsWith(ReqValue))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.toUpperCase().equals("CONTAINS"))
//        {
//            if(inField.contains(ReqValue))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else
//        {
//            logger.info("UN-RECOGNIZED OPERATOR VALUE [" + Operator + "]"); //Set to Logger
//        }
//        logger.info("Rule Not Applied");
//        return 0;
//    }



//    public static boolean ValidateTxn(Ifx ifx,String srcchannel,String destchannel) //(String institutionid, Ifx ifx)
//    {
//        //return 0 = No Rule Applied ; 1 = Rule Applied and Authorized ; -1 = Rule Applied and Not Authorized
//        //Ifx ifx = new Ifx(); //Raza for Testing
//        //ifx.setTrnType(TrnType.BALANCEINQUIRY); //Raza for Testing
//        //ifx.setAppPAN("1234567890123=1234"); //Raza for Testing
//        /*String query = "SELECT *\n" +
//                "FROM TXN_RULE_DEF\n" +
//                "INNER JOIN TXN_RULE\n" +
//                "ON txn_rule.RULEDEFID=TXN_RULE_DEF.RULEDEFID and txn_rule.trancode = '00' and txn_rule.msgtype = '0100'";
//
//        List<TxnRuleDef> result = null; //= GeneralDao.Instance.find(query);
//        result.addAll(GeneralDao.Instance.executeSqlQuery(query));*/
//
//        //Map<Integer, Currency> currencies = new HashMap<Integer, Currency>();
//
//        String query = "from "+ TxnRule.class.getName() + " m "
//                + " where m.srcchannel = :inchannel"
//                + " and m.TranType = :tran_type"; //Raza Make TranType condition as 'in' instead of '='
//
//        //logger.info("TranType [" + ifx.getTrnType().toString() + "]");
//        //logger.info("TranType [" + ifx.getTrnType().toString() + "]");
//        //logger.info("TranType [" + ifx.getTrnType().toString() + "]");
//        //System.out.println("TranType [" + ifx.getTrnType().toString() + "]");
//
//        HashMap<String, Object> parameters = new HashMap<String, Object>();
//        parameters.put("inchannel", srcchannel);
//        parameters.put("tran_type", ifx.getTrnType().toString());
//
//        List<TxnRule> RList = GeneralDao.Instance.find(query, parameters);
//
//        if(RList != null && RList.size() > 0)
//        {
//            for (TxnRule tRule : RList) {
//                System.out.println("**************************************************");
//                System.out.println("RuleDef ID [" + tRule.getRuleDef().getId() + "]");
//                System.out.println("RuleName [" + tRule.getRuleDef().getRuleName() + "]");
//                System.out.println("Field [" + tRule.getRuleDef().getField() + "]");
//                System.out.println("Value [" + tRule.getRuleDef().getValue() + "]");
//                System.out.println("Operator [" + tRule.getRuleDef().getOperator() + "]");
//                System.out.println("**************************************************");
//
//                String field = tRule.getRuleDef().getField();
//                String operator = tRule.getRuleDef().getOperator();
//                if(operator == null || field == null) //change it null then work in else
//                {
//                    System.out.println("Rule Not Defined Properly");
//                    return false;
//                }
//                else
//                {
//                    logger.info("Applying Rule... Field [" + field + "] operator [" + operator + "] Req Value [" + tRule.getRuleDef().getValue()  + "]");
//                        //if(operator.equals("=") || operator.equals("=="))
//                        //{
//                            if(field.equals("TRAN_CODE")) //DE-2
//                            {
//                                ValidateField(""+ifx.getTrnType().getType(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("AMOUNT_TRAN")) //DE-4
//                            {
//                                ValidateField(""+ifx.getAuth_Amt(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("IMD")) //DE-2 substr(0,6)
//                            {
//                                ValidateField(""+ifx.getAppPAN().substring(0,6),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("PAN")) //DE-2
//                            {
//                                ValidateField(""+ifx.getAppPAN(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("AMOUNT_SETT")) //DE-5
//                            {
//                                ValidateField(""+ifx.getSett_Amt(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("AMOUNT_CARD_BILL")) //DE-6
//                            {
//                                ValidateField(""+ifx.getSec_Amt(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("DATE_EXP")) //DE-14
//                            {
//                                ValidateField(""+ifx.getExpDt(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("DATE_SETT")) //DE-15
//                            {
//                                ValidateField(""+ifx.getSettleDt(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("ACQ_INS_CNTRY_CODE")) //DE-19
//                            {
//                                ValidateField(""+ifx.getMerchCountryCode(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("POS_ENT_MODE")) //DE-22
//                            {
//                                ValidateField(""+ifx.getPosEntryModeCode(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("POS_COND_CODE")) //DE-25
//                            {
//                                ValidateField(""+ifx.getPosConditionCode(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("POS_PIN_CAP_CODE")) //DE-26
//                            {
//                                ValidateField(""+ifx.getPosPinCaptureCode(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("ACQ_INS_ID")) //DE-32
//                            {
//                                ValidateField(""+ifx.getBankId(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("FWD_INS_ID")) //DE-33
//                            {
//                                ValidateField(""+ifx.getFwdBankId(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("CARD_ACCPT_TERM_ID")) //DE-41
//                            {
//                                ValidateField(""+ifx.getTerminalId(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("CARD_ACCPT_ID_CODE")) //DE-42
//                            {
//                                ValidateField(""+ifx.getOrgIdNum(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("CARD_ACCPT_NAME_LOC")) //DE-43
//                            {
//                                ValidateField(""+ifx.getCardAcceptNameLoc(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("CURRENCY_TRAN")) //DE-49
//                            {
//                                ValidateField(""+ifx.getAuth_Currency(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("CURRENCY_SETT")) //DE-50
//                            {
//                                ValidateField(""+ifx.getSett_Currency(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("CURRENCY_C_BILL")) //DE-51
//                            {
//                                ValidateField(""+ifx.getSec_Currency(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("POS_DATA_F60")) //DE-60
//                            {
//                                ValidateField(""+ifx.getSelfDefineData(),tRule.getRuleDef().getValue(),operator);
//                            }
//                            else if(field.equals("RECORD_DATA")) //DE-120
//                            {
//                                System.out.println("Implement whis Field accordingly");
//                            }
//                        //}
//                        //else if(operator.equals("!=") || operator.equals("<>"))
//                        //{}
//                        //else if(operator.equals(">"))
//                        //{}
//                        //else if(operator.equals(">="))
//                        //{}
//                        //else if(operator.equals("<"))
//                        //{}
//                        //else if(operator.equals("<="))
//                        //{}
//                        //else if(operator.toLowerCase().contains("substring"))
//                        //{
////                            int index1,index2,index3;
////                            index1 = operator.indexOf('(',0);
////                            index2 = operator.indexOf(',',0);
////                            index3 = operator.indexOf(')',0);
////
////                            String startindex = operator.substring(index1+1,index1+1+(index2-index1-1));
////                            String endindex = operator.substring(index2+1,index2+1+(index3-index2-1));
////
////                            System.out.println("startindex [" + startindex + "]");
////                            System.out.println("endindex [" + endindex + "]");
////                        }
////                        else if(operator.toUpperCase().equals("IS NULL"))
////                        {}
////                        else if(operator.toUpperCase().equals("IS NOT NULL"))
////                        {}
////                        else if(operator.toUpperCase().equals("IN"))
////                        {}
////                        else if(operator.toUpperCase().equals("NOT IN"))
////                        {}
////                        else if(operator.toUpperCase().equals("STARTS WITH"))
////                        {}
////                        else if(operator.toUpperCase().equals("ENDS WITH"))
////                        {}
////                        else if(operator.toUpperCase().equals("CONTAINS"))
////                        {}
////                        else
////                        {
////                            System.out.println("UN-RECOGNIZED OPERATOR VALUE [" + operator + "]"); //Set to Logger
////                        }
//                    //}
//                    //else
//                    //{
//                     //   System.out.println("UN-RECOGNIZED FIELD"); //Set to Logger
//                    //}
//                }
//            }
//        }
//        return false;
//    }

//    public static int ValidateField(String inField,String ReqValue,String Operator)
//    {
//        if(Operator.equals("=") || Operator.equals("==")) {
//            if(inField.equals(ReqValue))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.equals("!=") || Operator.equals("<>"))
//        {
//            if(!inField.equals(ReqValue))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.equals(">"))
//        {
//            Long LinField = Long.parseLong(inField);
//            Long LReqValue = Long.parseLong(ReqValue);
//            if(LinField > LReqValue)
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.equals(">="))
//        {
//            Long LinField = Long.parseLong(inField);
//            Long LReqValue = Long.parseLong(ReqValue);
//            if(LinField >= LReqValue)
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.equals("<"))
//        {
//            Long LinField = Long.parseLong(inField);
//            Long LReqValue = Long.parseLong(ReqValue);
//            if(LinField < LReqValue)
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.equals("<="))
//        {
//            Long LinField = Long.parseLong(inField);
//            Long LReqValue = Long.parseLong(ReqValue);
//            if(LinField <= LReqValue)
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.toLowerCase().contains("substring"))
//        {
//            int index1,index2,index3;
//            index1 = Operator.indexOf('(',0);
//            index2 = Operator.indexOf(',',0);
//            index3 = Operator.indexOf(')',0);
//
//            String startindex = Operator.substring(index1+1,index1+1+(index2-index1-1));
//            String endindex = Operator.substring(index2+1,index2+1+(index3-index2-1));
//
//            //System.out.println("startindex [" + startindex + "]");
//            //System.out.println("endindex [" + endindex + "]");
//        }
//        else if(Operator.toUpperCase().equals("IS NULL"))
//        {
//            if(inField == null || inField.equals(""))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.toUpperCase().equals("IS NOT NULL"))
//        {
//            if(inField != null || !inField.equals(""))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.toUpperCase().equals("IN"))
//        {
//            String FrstVal = "(" + inField + ","; //if first element
//            String MidVal = "," + inField + ","; //if Middle element
//            String LstVal = "," + inField + ")"; //if last element
//            String SnglVal = "(" + inField + ")"; //if only element
//
//            if(ReqValue.contains(FrstVal) || ReqValue.contains(MidVal) || ReqValue.contains(LstVal) || ReqValue.contains(SnglVal))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.toUpperCase().equals("NOT IN"))
//        {
//            String FrstVal = "(" + inField + ","; //if first element
//            String MidVal = "," + inField + ","; //if Middle element
//            String LstVal = "," + inField + ")"; //if last element
//            String SnglVal = "(" + inField + ")"; //if only element
//
//            if(ReqValue.contains(FrstVal) || ReqValue.contains(MidVal) || ReqValue.contains(LstVal) || ReqValue.contains(SnglVal))
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//            else
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//        }
//        else if(Operator.toUpperCase().equals("STARTS WITH"))
//        {
//            if(inField.startsWith(ReqValue))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.toUpperCase().equals("ENDS WITH"))
//        {
//            if(inField.endsWith(ReqValue))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else if(Operator.toUpperCase().equals("CONTAINS"))
//        {
//            if(inField.contains(ReqValue))
//            {
//                logger.info("Rule Passed");
//                return 1;
//            }
//            else
//            {
//                logger.info("Rule Failed");
//                return -1;
//            }
//        }
//        else
//        {
//            logger.info("UN-RECOGNIZED OPERATOR VALUE [" + Operator + "]"); //Set to Logger
//        }
//        logger.info("Rule Not Applied");
//        return 0;
//    }


    public TxnRuleCond getRuleCond() {
        return RuleCond;
    }

    public void setRuleCond(TxnRuleCond ruleCond) {
        RuleCond = ruleCond;
    }

    public String getRulename() {
        return Rulename;
    }

    public void setRulename(String rulename) {
        Rulename = rulename;
    }

    public String getSrcchannel() {
        return srcchannel;
    }

    public void setSrcchannel(String srcchannel) {
        this.srcchannel = srcchannel;
    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

//    public static String getKey(List<TxnRuleKey> inkeylist, Ifx ifx)
//    {
//        String Key="";
//        for(TxnRuleKey tulekey : inkeylist)
//        {
//            int field = Integer.parseInt(tulekey.getField());
//
//            switch(field)   //Note: field 0 is MTI; field 1 as IMD(start 6 digits of PAN)
//            {
//                case 0:
//                {
//                    if(Util.hasText(ifx.getMti()))
//                        Key += ifx.getMti() + "-";
//                    else
//                        Key += "*-";
//                }
//                case 1:
//                {
//                    if(Util.hasText(ifx.getAppPAN()))
//                        Key += ifx.getAppPAN().substring(0,7) + "-";
//                    else
//                        Key += "*-";
//                }
//                case 2:
//                {
//                    if(Util.hasText(ifx.getAppPAN()))
//                        Key += ifx.getAppPAN() + "-";
//                    else
//                        Key += "*-";
//                }
//                case 3: //using TRANTYPE instead of PROC_CODE
//                {
//                    if(Util.hasText(ifx.getTrnType().toString()))
//                        Key += ifx.getTrnType().toString() + "-";
//                    else
//                        Key += "*-";
//                }
////                case 4:
////                {
////                    if(ifx.getAuth_Amt() != null)
////                        Key += ifx.getAuth_Amt() + "-";
////                    else
////                        Key += "*-";
////                }
////                case 5:
////                {
////                    if(ifx.getSett_Amt() != null)
////                        Key += ifx.getSett_Amt() + "-";
////                    else
////                        Key += "*-";
////                }
////                case 6:
////                {
////                    if(ifx.getSec_Amt() != null)
////                        Key += ifx.getSec_Amt() + "-";
////                    else
////                        Key += "*-";
////                }
////                case 7:
////                {
////                    if(ifx.getDateLocalTran() != null)
////                        Key += ifx.getDateLocalTran() + "-";
////                    else
////                        Key += "*-";
////                }
//////                case 8:
//////                {
//////                    if(ifx.getCBILLfee != null)
//////                        Key += ifx.getCBILLfee + "-";
//////                    else
//////                        Key += "*-";
//////                }
////                case 9:
////                case 10:
////                case 11:
////                case 12:
////                case 13:
////                case 14:
////                case 15:
////                case 16:
////                case 17:
//                case 18:
//                {
//                    if(Util.hasText(ifx.getMerchantType()))
//                        Key += ifx.getMerchantType() + "-";
//                    else
//                        Key += "*-";
//                }
//                case 19:
//                {
//                    if(Util.hasText(ifx.getMerchCountryCode()))
//                        Key += ifx.getMerchCountryCode() + "-";
//                    else
//                        Key += "*-";
//                }
//                case 20:
//                {
//                    if(Util.hasText(ifx.getPanCountryCode()))
//                        Key += ifx.getPanCountryCode() + "-";
//                    else
//                        Key += "*-";
//                }
////                case 21:
////                {
////                    if(Util.hasText(ifx.getPanCountryCode()))
////                        Key += ifx.getPanCountryCode() + "-";
////                    else
////                        Key += "*-";
////                }
//                case 22:
//                {
//                    if(Util.hasText(ifx.getPosEntryModeCode()))
//                        Key += ifx.getPosEntryModeCode() + "-";
//                    else
//                        Key += "*-";
//                }
////                case 23:
////                case 24:
//                case 25:
//                {
//                    if(Util.hasText(ifx.getPosConditionCode()))
//                        Key += ifx.getPosConditionCode() + "-";
//                    else
//                        Key += "*-";
//                }
//                case 26:
//                {
//                    if(Util.hasText(ifx.getPosPinCaptureCode()))
//                        Key += ifx.getPosPinCaptureCode() + "-";
//                    else
//                        Key += "*-";
//                }
////                case 27:
////                case 28:
////                case 29:
////                case 30:
////                case 31:
//                case 32:
//                {
//                    if(Util.hasText(ifx.getBankId()))
//                        Key += ifx.getBankId() + "-";
//                    else
//                        Key += "*-";
//                }
//                case 33:
//                {
//                    if(Util.hasText(ifx.getFwdBankId()))
//                        Key += ifx.getFwdBankId() + "-";
//                    else
//                        Key += "*-";
//                }
////                case 34:
////                case 35:
////                case 36:
////                case 37:
////                case 38:
////                case 39:
////                case 40:
//                case 41:
//                {
//                    if(Util.hasText(ifx.getTerminalId()))
//                        Key += ifx.getTerminalId() + "-";
//                    else
//                        Key += "*-";
//                }
//                case 42:
//                {
//                    if(Util.hasText(ifx.getOrgIdNum()))
//                        Key += ifx.getOrgIdNum() + "-";
//                    else
//                        Key += "*-";
//                }
////                case 43:
////                case 44:
////                case 45:
////                case 46:
////                case 47:
////                case 48:
//                case 49:
//                {
//                    if(ifx.getAuth_Currency() != null)
//                        Key += ifx.getAuth_Currency() + "-";
//                    else
//                        Key += "*-";
//                }
//                case 50:
//                {
//                    if(ifx.getSett_Currency() != null)
//                        Key += ifx.getSett_Currency() + "-";
//                    else
//                        Key += "*-";
//                }
//                case 51:
//                {
//                    if(ifx.getSec_Currency() != null)
//                        Key += ifx.getSec_Currency() + "-";
//                    else
//                        Key += "*-";
//                }
////                case 52:
////                case 53:
////                case 54:
////                case 55:
////                case 56:
////                case 57:
////                case 58:
////                case 59:
////                case 60:
////                case 61:
////                case 62:
////                case 63:
////                case 64:
////                case 65:
////                case 66:
////                case 67:
////                case 68:
////                case 69:
////                case 70:
////                case 71:
////                case 72:
////                case 73:
////                case 74:
////                case 75:
////                case 76:
////                case 77:
////                case 78:
////                case 79:
////                case 80:
////                case 81:
////                case 82:
////                case 83:
////                case 84:
////                case 85:
////                case 86:
////                case 87:
////                case 88:
////                case 89:
////                case 90:
////                case 91:
////                case 92:
////                case 93:
////                case 94:
////                case 95:
////                case 96:
////                case 97:
////                case 98:
////                case 99:
////                case 100:
////                case 101:
////                case 102:
////                case 103:
////                case 104:
////                case 105:
////                case 106:
////                case 107:
////                case 108:
////                case 109:
////                case 110:
////                case 111:
////                case 112:
////                case 113:
////                case 114:
////                case 115:
////                case 116:
////                case 117:
////                case 118:
////                case 119:
////                case 120:
////                case 121:
////                case 122:
////                case 123:
////                case 124:
////                case 125:
////                case 126:
////                case 127:
////                case 128:
//                default:
//                    break;
//            }
//            Key = Key.substring(0,Key.length()-1); //Remove '-' char from end of Key
//        }
//        return "";
//    }
}

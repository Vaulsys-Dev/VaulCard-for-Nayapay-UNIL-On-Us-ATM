package vaulsys.authorization.impl;

import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.imp.Ifx;
import org.apache.log4j.Logger;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.List;

/**
 * Created by HP on 26-Oct-17.
 */
@Entity
@Table(name = "txn_rule_cond")
public class TxnRuleCond implements IEntity<Long> {

    @Transient
    static Logger logger = Logger.getLogger(TxnRuleCond.class); //For Logging

    @Id
    private Long RuleCondId;

    private String Field;

    private String Value;

    private String Operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RULEDEFID")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="RuleCondid_fk")
    private TxnRuleDef RuleDef;


    @Override
    public Long getId() {
        return RuleCondId;
    }

    @Override
    public void setId(Long id) {
        this.RuleCondId = id;
    }

    public String getField() {
        return Field;
    }

    public void setField(String field) {
        Field = field;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public String getOperator() {
        return Operator;
    }

    public void setOperator(String operator) {
        Operator = operator;
    }

    public TxnRuleDef getRuleDef() {
        return RuleDef;
    }

    public void setRuleDef(TxnRuleDef ruleDef) {
        RuleDef = ruleDef;
    }

//    public static boolean ValidateTxn(Ifx ifx, String srcchannel, String destchannel) //(String institutionid, Ifx ifx)
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
//        String query = "from "+ TxnRuleCond.class.getName() + " m "
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
//        List<TxnRuleCond> RList = GeneralDao.Instance.find(query, parameters);
//
//        if(RList != null && RList.size() > 0)
//        {
//            for (TxnRuleCond tRuleCond : RList) {
//                System.out.println("**************************************************");
//                System.out.println("RuleDef ID [" + tRuleCond.getRuleDef().getId() + "]");
//                System.out.println("RuleName [" + tRuleCond.getRuleDef().getRuleName() + "]");
//                System.out.println("Field [" + tRuleCond.getRuleDef().getField() + "]");
//                System.out.println("Value [" + tRuleCond.getRuleDef().getValue() + "]");
//                System.out.println("Operator [" + tRuleCond.getRuleDef().getOperator() + "]");
//                System.out.println("**************************************************");
//
//                String field = tRuleCond.getRuleDef().getField();
//                String operator = tRuleCond.getRuleDef().getOperator();
//                if(operator == null || field == null) //change it null then work in else
//                {
//                    System.out.println("Rule Not Defined Properly");
//                    return false;
//                }
//                else
//                {
//                    logger.info("Applying Rule... Field [" + field + "] operator [" + operator + "] Req Value [" + tRuleCond.getRuleDef().getValue()  + "]");
//                    //if(operator.equals("=") || operator.equals("=="))
//                    //{
//                    if(field.equals("TRAN_CODE")) //DE-2
//                    {
//                        ValidateField(""+ifx.getTrnType().getType(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("AMOUNT_TRAN")) //DE-4
//                    {
//                        ValidateField(""+ifx.getAuth_Amt(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("IMD")) //DE-2 substr(0,6)
//                    {
//                        ValidateField(""+ifx.getAppPAN().substring(0,6),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("PAN")) //DE-2
//                    {
//                        ValidateField(""+ifx.getAppPAN(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("AMOUNT_SETT")) //DE-5
//                    {
//                        ValidateField(""+ifx.getSett_Amt(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("AMOUNT_CARD_BILL")) //DE-6
//                    {
//                        ValidateField(""+ifx.getSec_Amt(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("DATE_EXP")) //DE-14
//                    {
//                        ValidateField(""+ifx.getExpDt(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("DATE_SETT")) //DE-15
//                    {
//                        ValidateField(""+ifx.getSettleDt(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("ACQ_INS_CNTRY_CODE")) //DE-19
//                    {
//                        ValidateField(""+ifx.getMerchCountryCode(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("POS_ENT_MODE")) //DE-22
//                    {
//                        ValidateField(""+ifx.getPosEntryModeCode(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("POS_COND_CODE")) //DE-25
//                    {
//                        ValidateField(""+ifx.getPosConditionCode(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("POS_PIN_CAP_CODE")) //DE-26
//                    {
//                        ValidateField(""+ifx.getPosPinCaptureCode(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("ACQ_INS_ID")) //DE-32
//                    {
//                        ValidateField(""+ifx.getBankId(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("FWD_INS_ID")) //DE-33
//                    {
//                        ValidateField(""+ifx.getFwdBankId(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("CARD_ACCPT_TERM_ID")) //DE-41
//                    {
//                        ValidateField(""+ifx.getTerminalId(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("CARD_ACCPT_ID_CODE")) //DE-42
//                    {
//                        ValidateField(""+ifx.getOrgIdNum(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("CARD_ACCPT_NAME_LOC")) //DE-43
//                    {
//                        ValidateField(""+ifx.getCardAcceptNameLoc(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("CURRENCY_TRAN")) //DE-49
//                    {
//                        ValidateField(""+ifx.getAuth_Currency(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("CURRENCY_SETT")) //DE-50
//                    {
//                        ValidateField(""+ifx.getSett_Currency(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("CURRENCY_C_BILL")) //DE-51
//                    {
//                        ValidateField(""+ifx.getSec_Currency(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("POS_DATA_F60")) //DE-60
//                    {
//                        ValidateField(""+ifx.getSelfDefineData(),tRuleCond.getRuleDef().getValue(),operator);
//                    }
//                    else if(field.equals("RECORD_DATA")) //DE-120
//                    {
//                        System.out.println("Implement whis Field accordingly");
//                    }
//                    //}
//                    //else if(operator.equals("!=") || operator.equals("<>"))
//                    //{}
//                    //else if(operator.equals(">"))
//                    //{}
//                    //else if(operator.equals(">="))
//                    //{}
//                    //else if(operator.equals("<"))
//                    //{}
//                    //else if(operator.equals("<="))
//                    //{}
//                    //else if(operator.toLowerCase().contains("substring"))
//                    //{
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
//                    //   System.out.println("UN-RECOGNIZED FIELD"); //Set to Logger
//                    //}
//                }
//            }
//        }
//        return false;
//    }
//
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

    public static int ValidateRuleConditions(List<TxnRuleCond> RList, Ifx ifx)
    {
        if(RList != null && RList.size() > 0)
        {
            for (TxnRuleCond tRuleCond : RList) {
                logger.info("**************************************************");
                logger.info("RuleDef ID [" + tRuleCond.getRuleDef().getId() + "]");
                logger.info("Cond Field [" + tRuleCond.getField() + "]");
                logger.info("Cond Operator [" + tRuleCond.getOperator() + "]");
                logger.info("Cond Value [" + tRuleCond.getValue() + "]");
                logger.info("**************************************************");

                String field = tRuleCond.getField();
                String operator = tRuleCond.getOperator();
                if(operator == null || field == null) //change it null then work in else
                {
                    System.out.println("Rule Not Defined Properly");
                    return 0;
                }
                else
                {
                    logger.info("Applying Rule... Field [" + field + "] operator [" + operator + "] Req Value [" + tRuleCond.getValue()  + "]");

                    switch(Integer.parseInt(field))
                    {
                        case 0: { //MTI
                            if (ValidateCond("" + ifx.getMti(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 1: { //IMD
                            if (ValidateCond("" + ifx.getAppPAN().substring(0,7), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 2: {
                            if (ValidateCond("" + ifx.getAppPAN(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 3: { //IMD
                            if (ValidateCond("" + ifx.getTrnType().toString(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 18: { //IMD
                            if (ValidateCond("" + ifx.getMerchantType(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 19: { //IMD
                            if (ValidateCond("" + ifx.getMerchCountryCode(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 20: { //IMD
                            if (ValidateCond("" + ifx.getPanCountryCode(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 22: { //IMD
                            if (ValidateCond("" + ifx.getPosEntryModeCode(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 25: { //IMD
                            if (ValidateCond("" + ifx.getPosConditionCode(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 26: { //IMD
                            if (ValidateCond("" + ifx.getPosPinCaptureCode(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 32: { //IMD
                            if (ValidateCond("" + ifx.getBankId(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 33: { //IMD
                            if (ValidateCond("" + ifx.getFwdBankId(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 41: { //IMD
                            if (ValidateCond("" + ifx.getTerminalId(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 42: { //IMD
                            if (ValidateCond("" + ifx.getOrgIdNum(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 49: { //IMD
                            if (ValidateCond("" + ifx.getAuth_Currency(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 50: { //IMD
                            if (ValidateCond("" + ifx.getSett_Currency(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        case 51: { //IMD
                            if (ValidateCond("" + ifx.getSec_Currency(), tRuleCond.getValue(), operator) < 0)
                                return -1;
                            else
                            {
                                try { //seperate try catch for each Rule
                                    ifx = TxnRuleDef.ApplyRule(tRuleCond.getRuleDef().getField(), tRuleCond.getRuleDef().getValue(), tRuleCond.getRuleDef().getOperator(), ifx);
                                }
                                catch (Exception e)
                                {
                                    logger.info("Exception caught while applying Rule");
                                }
                            }
                        }
                        break;
                        default:
                            break;
                    }
                    return 1;

//                    if(field.equals("TRAN_CODE")) //DE-2
//                    {
//                        if(ValidateCond(""+ifx.getTrnType().getType(),tRuleCond.getValue(),operator) < 0)
//                        return -1;
//                    }
//                    else if(field.equals("AMOUNT_TRAN")) //DE-4
//                    {
//                        if(ValidateCond(""+ifx.getAuth_Amt(),tRuleCond.getValue(),operator)< 0)
//                            return -1;
//                    }
//                    else if(field.equals("IMD")) //DE-2 substr(0,6)
//                    {
//                        if(ValidateCond(""+ifx.getAppPAN().substring(0,6),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("PAN")) //DE-2
//                    {
//                        if(ValidateCond(""+ifx.getAppPAN(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//
//                    }
//                    else if(field.equals("AMOUNT_SETT")) //DE-5
//                    {
//                        if(ValidateCond(""+ifx.getSett_Amt(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("AMOUNT_CARD_BILL")) //DE-6
//                    {
//                        if(ValidateCond(""+ifx.getSec_Amt(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("DATE_EXP")) //DE-14
//                    {
//                        if(ValidateCond(""+ifx.getExpDt(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("DATE_SETT")) //DE-15
//                    {
//                        if(ValidateCond(""+ifx.getSettleDt(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("ACQ_INS_CNTRY_CODE")) //DE-19
//                    {
//                        if(ValidateCond(""+ifx.getMerchCountryCode(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("POS_ENT_MODE")) //DE-22
//                    {
//                        if(ValidateCond(""+ifx.getPosEntryModeCode(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("POS_COND_CODE")) //DE-25
//                    {
//                        if(ValidateCond(""+ifx.getPosConditionCode(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("POS_PIN_CAP_CODE")) //DE-26
//                    {
//                        if(ValidateCond(""+ifx.getPosPinCaptureCode(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("ACQ_INS_ID")) //DE-32
//                    {
//                        if(ValidateCond(""+ifx.getBankId(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("FWD_INS_ID")) //DE-33
//                    {
//                        if(ValidateCond(""+ifx.getFwdBankId(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("CARD_ACCPT_TERM_ID")) //DE-41
//                    {
//                        if(ValidateCond(""+ifx.getTerminalId(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("CARD_ACCPT_ID_CODE")) //DE-42
//                    {
//                        if(ValidateCond(""+ifx.getOrgIdNum(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("CARD_ACCPT_NAME_LOC")) //DE-43
//                    {
//                        if(ValidateCond(""+ifx.getCardAcceptNameLoc(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("CURRENCY_TRAN")) //DE-49
//                    {
//                        if(ValidateCond(""+ifx.getAuth_Currency(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("CURRENCY_SETT")) //DE-50
//                    {
//                        if(ValidateCond(""+ifx.getSett_Currency(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("CURRENCY_C_BILL")) //DE-51
//                    {
//                        if(ValidateCond(""+ifx.getSec_Currency(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("POS_DATA_F60")) //DE-60
//                    {
//                        if(ValidateCond(""+ifx.getSelfDefineData(),tRuleCond.getValue(),operator) < 0)
//                            return -1;
//                    }
//                    else if(field.equals("RECORD_DATA")) //DE-120
//                    {
//                        System.out.println("Implement whis Field accordingly");
//                    }
                    //Now apply rule after all conditions are verified and not returned -1
                }
            }
        }
        return 0;
    }

    public static int ValidateCond(String inField,String ReqValue,String Operator)
    {
        if(Operator.equals("=") || Operator.equals("==")) {
            if(inField.equals(ReqValue))
            {
                logger.info("Rule Passed");
                return 1;
            }
            else
            {
                logger.info("Rule Failed");
                return -1;
            }
        }
        else if(Operator.equals("!=") || Operator.equals("<>"))
        {
            if(!inField.equals(ReqValue))
            {
                logger.info("Rule Passed");
                return 1;
            }
            else
            {
                logger.info("Rule Failed");
                return -1;
            }
        }
        else if(Operator.equals(">"))
        {
            Long LinField = Long.parseLong(inField);
            Long LReqValue = Long.parseLong(ReqValue);
            if(LinField > LReqValue)
            {
                logger.info("Rule Passed");
                return 1;
            }
            else
            {
                logger.info("Rule Failed");
                return -1;
            }
        }
        else if(Operator.equals(">="))
        {
            Long LinField = Long.parseLong(inField);
            Long LReqValue = Long.parseLong(ReqValue);
            if(LinField >= LReqValue)
            {
                logger.info("Rule Passed");
                return 1;
            }
            else
            {
                logger.info("Rule Failed");
                return -1;
            }
        }
        else if(Operator.equals("<"))
        {
            Long LinField = Long.parseLong(inField);
            Long LReqValue = Long.parseLong(ReqValue);
            if(LinField < LReqValue)
            {
                logger.info("Rule Passed");
                return 1;
            }
            else
            {
                logger.info("Rule Failed");
                return -1;
            }
        }
        else if(Operator.equals("<="))
        {
            Long LinField = Long.parseLong(inField);
            Long LReqValue = Long.parseLong(ReqValue);
            if(LinField <= LReqValue)
            {
                logger.info("Rule Passed");
                return 1;
            }
            else
            {
                logger.info("Rule Failed");
                return -1;
            }
        }
        else if(Operator.toLowerCase().contains("substring"))
        {
            int index1,index2,index3;
            index1 = Operator.indexOf('(',0);
            index2 = Operator.indexOf(',',0);
            index3 = Operator.indexOf(')',0);

            String startindex = Operator.substring(index1+1,index1+1+(index2-index1-1));
            String endindex = Operator.substring(index2+1,index2+1+(index3-index2-1));

            //System.out.println("startindex [" + startindex + "]");
            //System.out.println("endindex [" + endindex + "]");
        }
        else if(Operator.toUpperCase().equals("IS NULL"))
        {
            if(inField == null || inField.equals(""))
            {
                logger.info("Rule Passed");
                return 1;
            }
            else
            {
                logger.info("Rule Failed");
                return -1;
            }
        }
        else if(Operator.toUpperCase().equals("IS NOT NULL"))
        {
            if(inField != null || !inField.equals(""))
            {
                logger.info("Rule Passed");
                return 1;
            }
            else
            {
                logger.info("Rule Failed");
                return -1;
            }
        }
        else if(Operator.toUpperCase().equals("IN"))
        {
            String FrstVal = "(" + inField + ","; //if first element
            String MidVal = "," + inField + ","; //if Middle element
            String LstVal = "," + inField + ")"; //if last element
            String SnglVal = "(" + inField + ")"; //if only element

            if(ReqValue.contains(FrstVal) || ReqValue.contains(MidVal) || ReqValue.contains(LstVal) || ReqValue.contains(SnglVal))
            {
                logger.info("Rule Passed");
                return 1;
            }
            else
            {
                logger.info("Rule Failed");
                return -1;
            }
        }
        else if(Operator.toUpperCase().equals("NOT IN"))
        {
            String FrstVal = "(" + inField + ","; //if first element
            String MidVal = "," + inField + ","; //if Middle element
            String LstVal = "," + inField + ")"; //if last element
            String SnglVal = "(" + inField + ")"; //if only element

            if(ReqValue.contains(FrstVal) || ReqValue.contains(MidVal) || ReqValue.contains(LstVal) || ReqValue.contains(SnglVal))
            {
                logger.info("Rule Failed");
                return -1;
            }
            else
            {
                logger.info("Rule Passed");
                return 1;
            }
        }
        else if(Operator.toUpperCase().equals("STARTS WITH"))
        {
            if(inField.startsWith(ReqValue))
            {
                logger.info("Rule Passed");
                return 1;
            }
            else
            {
                logger.info("Rule Failed");
                return -1;
            }
        }
        else if(Operator.toUpperCase().equals("ENDS WITH"))
        {
            if(inField.endsWith(ReqValue))
            {
                logger.info("Rule Passed");
                return 1;
            }
            else
            {
                logger.info("Rule Failed");
                return -1;
            }
        }
        else if(Operator.toUpperCase().equals("CONTAINS"))
        {
            if(inField.contains(ReqValue))
            {
                logger.info("Rule Passed");
                return 1;
            }
            else
            {
                logger.info("Rule Failed");
                return -1;
            }
        }
        else
        {
            logger.info("UN-RECOGNIZED OPERATOR VALUE [" + Operator + "]"); //Set to Logger
        }
        logger.info("Rule Not Applied");
        return -1;
    }
}

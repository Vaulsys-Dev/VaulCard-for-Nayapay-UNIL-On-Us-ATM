package vaulsys.authorization.impl;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Created by Raza on 23-Feb-17.
 */
@Entity
@Table(name = "txn_rule_def")
public class TxnRuleDef implements IEntity<Long> {

    @Transient
    static Logger logger = Logger.getLogger(TxnRuleDef.class); //For Logging

    @Id
    private Long RuleDefId;

    private String Field;

    private String Value;

    private String Operator;

    public Long getId() {
        return RuleDefId;
    }

    public void setId(Long ruledefid) {
        this.RuleDefId = ruledefid;
    }

    public String getField() {
        return this.Field;
    }

    public void setField(String field) {
        this.Field = field;
    }

    public String getValue() {
        return this.Value;
    }

    public void setValue(String value) {
        this.Value = value;
    }

    public String getOperator() {
        return this.Operator;
    }

    public void setOperator(String opt) {
        this.Operator = opt;
    }

    public static Ifx ApplyRule(String inField, String ReqValue, String Operator, Ifx ifx) throws AuthorizationException {
        try {
            int field = Integer.parseInt(inField);

            switch (Integer.parseInt(inField)) {
                case 0: {

                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setMti(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                        String sfield = ifx.getMti();
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                        ifx.setMti(sfield.substring(Integer.parseInt(startindex), Integer.parseInt(endindex)));

                    } else {
                        logger.info("UN-RECOGNIZED OPERATOR VALUE [" + Operator + "]"); //Set to Logger
                    }
                }
                break;
                case 1: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setMti(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                        String sfield = ifx.getAppPAN().substring(0, 7);
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                        sfield = sfield.substring(Integer.parseInt(startindex), Integer.parseInt(endindex));
                        ifx.setAppPAN(ifx.getAppPAN().replaceAll(ifx.getAppPAN().substring(0, 7), sfield));
                    }
                }
                break;
                case 2: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setAppPAN(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                        String sfield = ifx.getAppPAN();
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        sfield = sfield.substring(Integer.parseInt(startindex), Integer.parseInt(endindex));
                        ifx.setAppPAN(ifx.getAppPAN().replaceAll(ifx.getAppPAN(), sfield));
                    }
                }
                break;
                case 3: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        //ifx.setTrnType(ReqValue);

                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    }
                }
                break;
                case 18: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setMerchantType(ReqValue);

                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    }
                }
                break;
                case 19: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setMerchCountryCode(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    }
                }
                break;
                case 20: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setPanCountryCode(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    }
                }
                break;
                case 22: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setPosEntryModeCode(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    }
                }
                break;
                case 25: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setPosConditionCode(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    }
                }
                break;
                case 26: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setPosPinCaptureCode(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    }
                }
                break;
                case 32: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setBankId(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    }
                }
                break;
                case 33: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setFwdBankId(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    }
                }
                break;
                case 39: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setRsCode(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    }
                }
                break;
                case 41: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setTerminalId(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                    }
                }
                break;
                case 42: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setOrgIdNum(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    }
                }
                break;
                case 49: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setAuth_Currency(Integer.parseInt(ReqValue));
                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    }
                }
                break;
                case 50: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setSett_Currency(ReqValue);
                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    }
                }
                break;
                case 51: {
                    if (Operator.equals("=") || Operator.equals("ASSIGN")) {
                        ifx.setSec_Currency(Integer.parseInt(ReqValue));
                    } else if (Operator.toLowerCase().contains("substring")) {
                        int index1, index2, index3;
                        index1 = Operator.indexOf('(', 0);
                        index2 = Operator.indexOf(',', 0);
                        index3 = Operator.indexOf(')', 0);

                        String startindex = Operator.substring(index1 + 1, index1 + 1 + (index2 - index1 - 1));
                        String endindex = Operator.substring(index2 + 1, index2 + 1 + (index3 - index2 - 1));

                        //System.out.println("startindex [" + startindex + "]");
                        //System.out.println("endindex [" + endindex + "]");
                    } else {
                        logger.info("UN-RECOGNIZED OPERATOR VALUE [" + Operator + "]"); //Set to Logger
                    }
                }
                break;
                default:
                    break;

//        logger.info("Rule Not Applied");
//        return 0;
            }
            GeneralDao.Instance.saveOrUpdate(ifx);
            return ifx;
        }
        catch(Exception e)
        {
            logger.info("Exception caught while applyinh rule...");
            e.printStackTrace();
            return ifx;
        }
        }

}

package vaulsys.authorization.impl;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;
import org.apache.log4j.Logger;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * Created by Raza on 26-Oct-17.
 */
@Entity
@Table(name = "txn_rule_key")
public class TxnRuleKey implements IEntity<Long> {

    @Transient
    static Logger logger = Logger.getLogger(TxnRuleKey.class); //For Logging

    @Id
    private Long id;

    private String channel;

    private String Field;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getField() {
        return Field;
    }

    public void setField(String field) {
        Field = field;
    }

    public static String getKey(List<TxnRuleKey> inkeylist, Ifx ifx)
    {
        String Key="";
        for(TxnRuleKey tulekey : inkeylist)
        {
            int field = Integer.parseInt(tulekey.getField());

            switch(field)   //Note: field 0 is MTI; field 1 as IMD(start 6 digits of PAN)
            {
                case 0:
                {
                    if(Util.hasText(ifx.getMti()))
                        Key += ifx.getMti() + "-";
                    else
                        Key += "*-";
                }
                break;
                case 1: //IMD
                {
                    if(Util.hasText(ifx.getAppPAN()))
                        Key += ifx.getAppPAN().substring(0,7) + "-";
                    else
                        Key += "*-";
                }
                break;
                case 2:
                {
                    if(Util.hasText(ifx.getAppPAN()))
                        Key += ifx.getAppPAN() + "-";
                    else
                        Key += "*-";
                }
                break;
                case 3: //using TRANTYPE instead of PROC_CODE
                {
                    if(Util.hasText(ifx.getTrnType().toString()))
                        Key += ifx.getTrnType().toString() + "-";
                    else
                        Key += "*-";
                }
                break;
//                case 4:
//                {
//                    if(ifx.getAuth_Amt() != null)
//                        Key += ifx.getAuth_Amt() + "-";
//                    else
//                        Key += "*-";
//                }
//                case 5:
//                {
//                    if(ifx.getSett_Amt() != null)
//                        Key += ifx.getSett_Amt() + "-";
//                    else
//                        Key += "*-";
//                }
//                case 6:
//                {
//                    if(ifx.getSec_Amt() != null)
//                        Key += ifx.getSec_Amt() + "-";
//                    else
//                        Key += "*-";
//                }
//                case 7:
//                {
//                    if(ifx.getDateLocalTran() != null)
//                        Key += ifx.getDateLocalTran() + "-";
//                    else
//                        Key += "*-";
//                }
////                case 8:
////                {
////                    if(ifx.getCBILLfee != null)
////                        Key += ifx.getCBILLfee + "-";
////                    else
////                        Key += "*-";
////                }
//                case 9:
//                case 10:
//                case 11:
//                case 12:
//                case 13:
//                case 14:
//                case 15:
//                case 16:
//                case 17:
                case 18:
                {
                    if(Util.hasText(ifx.getMerchantType()))
                        Key += ifx.getMerchantType() + "-";
                    else
                        Key += "*-";
                }
                break;
                case 19:
                {
                    if(Util.hasText(ifx.getMerchCountryCode()))
                        Key += ifx.getMerchCountryCode() + "-";
                    else
                        Key += "*-";
                }
                break;
                case 20:
                {
                    if(Util.hasText(ifx.getPanCountryCode()))
                        Key += ifx.getPanCountryCode() + "-";
                    else
                        Key += "*-";
                }
                break;
//                case 21:
//                {
//                    if(Util.hasText(ifx.getPanCountryCode()))
//                        Key += ifx.getPanCountryCode() + "-";
//                    else
//                        Key += "*-";
//                }
                case 22:
                {
                    if(Util.hasText(ifx.getPosEntryModeCode()))
                        Key += ifx.getPosEntryModeCode() + "-";
                    else
                        Key += "*-";
                }
                break;
//                case 23:
//                case 24:
                case 25:
                {
                    if(Util.hasText(ifx.getPosConditionCode()))
                        Key += ifx.getPosConditionCode() + "-";
                    else
                        Key += "*-";
                }
                break;
                case 26:
                {
                    if(Util.hasText(ifx.getPosPinCaptureCode()))
                        Key += ifx.getPosPinCaptureCode() + "-";
                    else
                        Key += "*-";
                }
                break;
//                case 27:
//                case 28:
//                case 29:
//                case 30:
//                case 31:
                case 32:
                {
                    if(Util.hasText(ifx.getBankId()))
                        Key += ifx.getBankId() + "-";
                    else
                        Key += "*-";
                }
                break;
                case 33:
                {
                    if(Util.hasText(ifx.getFwdBankId()))
                        Key += ifx.getFwdBankId() + "-";
                    else
                        Key += "*-";
                }
                break;
//                case 34:
//                case 35:
//                case 36:
//                case 37:
//                case 38:
//                case 39:
//                case 40:
                case 41:
                {
                    if(Util.hasText(ifx.getTerminalId()))
                        Key += ifx.getTerminalId() + "-";
                    else
                        Key += "*-";
                }
                break;
                case 42:
                {
                    if(Util.hasText(ifx.getOrgIdNum()))
                        Key += ifx.getOrgIdNum() + "-";
                    else
                        Key += "*-";
                }
                break;
//                case 43:
//                case 44:
//                case 45:
//                case 46:
//                case 47:
//                case 48:
                case 49:
                {
                    if(ifx.getAuth_Currency() != null)
                        Key += ifx.getAuth_Currency() + "-";
                    else
                        Key += "*-";
                }
                break;
                case 50:
                {
                    if(ifx.getSett_Currency() != null)
                        Key += ifx.getSett_Currency() + "-";
                    else
                        Key += "*-";
                }
                break;
                case 51:
                {
                    if(ifx.getSec_Currency() != null)
                        Key += ifx.getSec_Currency() + "-";
                    else
                        Key += "*-";
                }
                break;
//                case 52:
//                case 53:
//                case 54:
//                case 55:
//                case 56:
//                case 57:
//                case 58:
//                case 59:
//                case 60:
//                case 61:
//                case 62:
//                case 63:
//                case 64:
//                case 65:
//                case 66:
//                case 67:
//                case 68:
//                case 69:
//                case 70:
//                case 71:
//                case 72:
//                case 73:
//                case 74:
//                case 75:
//                case 76:
//                case 77:
//                case 78:
//                case 79:
//                case 80:
//                case 81:
//                case 82:
//                case 83:
//                case 84:
//                case 85:
//                case 86:
//                case 87:
//                case 88:
//                case 89:
//                case 90:
//                case 91:
//                case 92:
//                case 93:
//                case 94:
//                case 95:
//                case 96:
//                case 97:
//                case 98:
//                case 99:
//                case 100:
//                case 101:
//                case 102:
//                case 103:
//                case 104:
//                case 105:
//                case 106:
//                case 107:
//                case 108:
//                case 109:
//                case 110:
//                case 111:
//                case 112:
//                case 113:
//                case 114:
//                case 115:
//                case 116:
//                case 117:
//                case 118:
//                case 119:
//                case 120:
//                case 121:
//                case 122:
//                case 123:
//                case 124:
//                case 125:
//                case 126:
//                case 127:
//                case 128:
                default:
                    break;
            }
        }
        Key = Key.substring(0,Key.length()-1); //Remove '-' char from end of Key
        return Key;
    }
}

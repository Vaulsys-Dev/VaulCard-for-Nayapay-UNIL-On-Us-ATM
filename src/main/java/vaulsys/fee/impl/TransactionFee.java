package vaulsys.fee.impl;

import vaulsys.fee.base.FeeInfo;
import vaulsys.protocols.ifx.imp.Ifx;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;
import java.util.Set;

@Entity
@DiscriminatorValue(value = "TransactionFee")
public class TransactionFee extends BaseFee {

    public TransactionFee() {
        super();
    }

    public TransactionFee(String rule, Set<FeeItem> feeItemList,
                          boolean isEnabled) {
        super(rule, feeItemList, isEnabled);
    }

    public TransactionFee(String rule, Set<FeeItem> feeItemList,
                          boolean isEnabled, String description) {
        super(rule, feeItemList, description, isEnabled);
    }

    public TransactionFee(String rule, Set<FeeItem> feeItemList) {
        super(rule, feeItemList, true);
    }

    public TransactionFee(String rule, Set<FeeItem> feeItemList,
                          String description) {
        super(rule, feeItemList, description, true);
    }

    public List<FeeInfo> feeCalculator(Ifx ifx) {
        return super.calculate(ifx);
    }

//    @Override
//    protected String putField(Object data, String rule) {
//        Ifx ifx = (Ifx) data;
//        int ind;
//        while ((ind = rule.indexOf("ifx.")) >= 0) {
//            String s = rule.substring(ind);
//            int si = s.indexOf(' ') == -1 ? s.length() : s.indexOf(' ');
//            int sa = s.indexOf('(') == -1 ? s.length() : s.indexOf('(');
//            int sb = s.indexOf(')') == -1 ? s.length() : s.indexOf(')');
//
//            si = Math.min(si, sa);
//            si = Math.min(si, sb);
//
//            for (String s2 : Operator.operators) {
//                int sx = s.indexOf(s2) == -1 ? s.length() : s.indexOf(s2);
//                si = Math.min(si, sx);
//            }
//            String expr = rule.substring(ind, ind + si);
//            String value = ifx.get(expr).toString() + " ";
//            rule = rule.replaceAll(expr, value);
//        }
//        return rule;
//    }
//
//    protected String putField(Ifx ifx, String rule) {
//        int ind;
//        while ((ind = rule.indexOf("ifx.")) >= 0) {
//            String s = rule.substring(ind);
//            int si = s.indexOf(' ') == -1 ? s.length() : s.indexOf(' ');
//            int sa = s.indexOf('(') == -1 ? s.length() : s.indexOf('(');
//            int sb = s.indexOf(')') == -1 ? s.length() : s.indexOf(')');
//
//            si = Math.min(si, sa);
//            si = Math.min(si, sb);
//
//            for (String s2 : Operator.operators) {
//                int sx = s.indexOf(s2) == -1 ? s.length() : s.indexOf(s2);
//                si = Math.min(si, sx);
//            }
//            String expr = rule.substring(ind, ind + si);
//            String value = ifx.get(expr).toString() + " ";
//            rule = rule.replaceAll(expr, value);
//        }
//        return rule;
//    }

    public boolean isIsAllowedCard() {
        return super.getIsAllowedCard() == null ? false : super.getIsAllowedCard();
    }

    public boolean isRefundable() {
        return super.getRefundable() == null ? false : super.getRefundable();
    }
}

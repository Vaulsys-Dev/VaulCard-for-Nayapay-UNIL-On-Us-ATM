package vaulsys.fee.base;

import vaulsys.entity.impl.FinancialEntity;
import vaulsys.fee.impl.FeeItem;

public class FeeInfo {

    private FinancialEntity entityToBeCredited;

    private FinancialEntity entityToBeDebited;

    private long amount;

    private String description;

    private FeeItem feeItem;

    public FeeInfo() {
    }

    public FeeInfo(FinancialEntity entityToBeCredited, FinancialEntity entityToBeDebited, long amount, String description) {
        this.entityToBeCredited = entityToBeCredited;
        this.entityToBeDebited = entityToBeDebited;
        this.amount = amount;
        this.description = description;
    }

    public FeeInfo(FinancialEntity entityToBeCredited, FinancialEntity entityToBeDebited, long amount, FeeItem feeItem, String description) {
        this.entityToBeCredited = entityToBeCredited;
        this.entityToBeDebited = entityToBeDebited;
        this.amount = amount;
        this.feeItem = feeItem;
        this.description = description;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FinancialEntity getEntityToBeCredited() {
        return entityToBeCredited;
    }

    public void setEntityToBeCredited(FinancialEntity entityToBeCredited) {
        this.entityToBeCredited = entityToBeCredited;
    }

    public FinancialEntity getEntityToBeDebited() {
        return entityToBeDebited;
    }

    public void setEntityToBeDebited(FinancialEntity entityToBeDebited) {
        this.entityToBeDebited = entityToBeDebited;
    }

    public FeeItem getFeeItem() {
        return feeItem;
    }

    public void setFeeItem(FeeItem feeItem) {
        this.feeItem = feeItem;
    }
}

package vaulsys.customer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CardGroupHierarchyVO implements Serializable {
    public static enum RSCodes {
        Success(1),
        LoginFaild(2),
        BusinessException(3),
        SystemException(4),
        SecurityException(5),
        CoreTimeOutException(6),
        InvalidInputData(7),
        CoreResponseException(8),
        CyberCardCountLimited(9);


        private Integer id;

        RSCodes(Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }
    }

    public static enum InputData {
        General(1),
        CardPAN(2),
        DateFrom(3),
        DateTo(4),
        Amount(5),
        DepositNumber(6),
        GreatAmount(7),
        CardServiceValueObject_cycleCount(8),
        CardServiceValueObject_type(9),
        CardServiceValueObject_filterList1(10),
        CardServiceValueObject_filterList2(11),
        CardServiceValueObject_currency(12),
        CardServiceValueObject_cycle(13),
        CardServiceValueObject_valueOfOnUs(14),
        CardServiceValueObject_applyTransactionFilter(15),
        CardServiceValueObject_amountOrCount(16),
        CardServiceId(17);

        private Integer id;

        InputData(Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }
    }

    private RSCodes rsCode;
    private String message;
    private InputData inputData;
    private String coreRSCode;
    List<Long> cardGroups;

    public CardGroupHierarchyVO() {
        rsCode = RSCodes.Success;
        message = "";
        inputData = InputData.General;
        coreRSCode = "";
        cardGroups = new ArrayList<Long>();
    }
    
    
    public RSCodes getRsCode() {
        return rsCode;
    }

    public void setRsCode(RSCodes rsCode) {
        this.rsCode = rsCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public InputData getInputData() {
        return inputData;
    }

    public void setInputData(InputData inputData) {
        this.inputData = inputData;
    }

    public String getCoreRSCode() {
        return coreRSCode;
    }

    public void setCoreRSCode(String coreRSCode) {
        this.coreRSCode = coreRSCode;
    }
    
    public List<Long> getCardGroups() {
        return cardGroups;
    }

    public void setCardGroups(List<Long> cardGroups) {
        this.cardGroups = cardGroups;
    }
}

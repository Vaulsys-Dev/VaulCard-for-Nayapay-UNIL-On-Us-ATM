package vaulsys.clearing.base;

public class ClearingAction {

    private int type;


    protected ClearingAction(int type) {
        this.type = type;
    }

    public static ClearingAction UNKNOWN = new ClearingAction(0);
    public static ClearingAction COUTOVER_REQUEST = new ClearingAction(1);
    public static ClearingAction COUTOVER_RESPONSE = new ClearingAction(2);
    public static ClearingAction RECONCILEMNET_REQUEST = new ClearingAction(3);
    public static ClearingAction ACQUIRER_RECONCILEMNET_RESPONSE = new ClearingAction(4);
    public static ClearingAction ISSUER_RECONCILEMNET_RESPONSE = new ClearingAction(5);
    public static ClearingAction ACQUIRER_FINALIZE_RECONCILEMNET = new ClearingAction(6);
    public static ClearingAction ISSUER_FINALIZE_RECONCILEMNET = new ClearingAction(7);


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClearingAction that = (ClearingAction) o;

        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type;
    }

}

package vaulsys.util.coreLoadBalancer;

public enum CoreServerUsageType {
    Normal(1),
    Reversal(2),
    UserManagement(3);

    private int intValue;

    private CoreServerUsageType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static CoreServerUsageType convert(int i) {
        for (CoreServerUsageType coreServerUsageType : values()) {
            if (coreServerUsageType.getValue() == i) {
                return coreServerUsageType;
            }
        }
        return null;
    }

    public static Integer getInteger(String s) {
        return Enum.valueOf(CoreServerUsageType.class, s).getValue();
    }

    public static String getString(int i) {
        return convert(i).toString();
    }
}

package vaulsys.util;

public class MyLong {
    public long value;

    public MyLong(MyLong longv) {
        this.value = longv.value;
    }

    public MyLong(long val) {
        value = val;
    }

    public String toString() {
        return "" + value;
    }
}

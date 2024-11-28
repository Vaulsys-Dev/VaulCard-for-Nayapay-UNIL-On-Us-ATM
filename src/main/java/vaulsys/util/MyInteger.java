package vaulsys.util;

public class MyInteger {
    public int value;

    public MyInteger(MyInteger integer) {
        this.value = integer.value;
    }

    public MyInteger(int val) {
        value = val;
    }

    public String toString() {
        return "" + value;
    }
}

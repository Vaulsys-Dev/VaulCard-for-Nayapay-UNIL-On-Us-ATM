package vaulsys.terminal.atm;

public class DynamicCashDispencer {

    private static DynamicCashDispencer instance;

    public static DynamicCashDispencer getInstance() {
        if (instance == null)
            instance = new DynamicCashDispencer();
        return instance;
    }

    private class State {
        public State(long amount, int kSize, int inUse) {
            this.amount = amount;
            this.k = new int[kSize];
            this.lastCoinIndex = inUse;
        }

        public State(long amount, int[] k, int inUse) {
            this.amount = amount;
            this.k = new int[k.length];
            System.arraycopy(k, 0, this.k, 0, k.length);
            this.lastCoinIndex = inUse;
        }

        public State useCoin(int index, int count) {
            if (this.k[index] < count)
                return null;
            this.k[index] -= count;
            return this;
        }

        public long amount;
        public int[] k;
        int lastCoinIndex; // the index of the last coin we've used
    }

    private int[] result;

    private long gcd(long number1, long number2) {
        if (number1 > number2)
            return gcd(number2, number1); // number1 should be less than number2
        if (number1 == 0)
            return number2;
        return gcd(number2 % number1, number1);
    }

    public int[] dispense(long amount, long[] values, int[] numbers) throws Exception {
        long[] c = new long[values.length];
        int[] k = new int[numbers.length];
        System.arraycopy(values, 0, c, 0, c.length);
        System.arraycopy(numbers, 0, k, 0, k.length);

        if (c.length != k.length)
            return null;
        int n = c.length;

        long g = c[0];
        for (int i = 1; i < n; i++)
            g = gcd(g, c[i]);
        if (amount % g != 0)
            return null;
        // throw new Exception("Impossible to dispense");

        amount /= g;
        for (int i = 0; i < n; i++)
            c[i] /= g;

        long total = 0L;
        for (int i = 0; i < n; i++)
            total += (long) (c[i]) * (long) (k[i]);
        if (total < amount)
            return null;
        // throw new Exception("Not enough money to dispense");

        result = new int[n];
        for (int i = 0; i < n; i++)
            result[i] = 0;

        // falseStates = new HashSet<State>();

        int index = 0;
        for(;k[index]<=0; index++);
        	
        long lcm = c[index];
        for (int i = index+1; i < n; i++)
            if (k[i] > 0)
                lcm = (c[i] * lcm) / gcd(c[i], lcm);

        int biggestCoinIndex = c.length - 1;
        while (biggestCoinIndex >= 0 && k[biggestCoinIndex] < lcm / c[biggestCoinIndex])
            biggestCoinIndex--;
        while (amount >= 0 && amount > 3 * lcm) {
            while (biggestCoinIndex >= 0 && k[biggestCoinIndex] < lcm / c[biggestCoinIndex])
                biggestCoinIndex--;
            if (biggestCoinIndex < 0)
                break;
            amount -= lcm;
            k[biggestCoinIndex] -= lcm / c[biggestCoinIndex];
            result[biggestCoinIndex] += lcm / c[biggestCoinIndex];
        }
        if (recDispence(c, new State(amount, k, n - 1)))
            return result;
        else
            return null;
    }// end of dispense method

    private boolean recDispence(long[] coin, State state) {
        if (state.amount == 0)
            return true;
        for (int i = state.lastCoinIndex; i >= 0; i--)
            if ((state.k[i] > 0 && coin[i] <= state.amount)
                    && (recDispence(coin, (new State(state.amount - coin[i], state.k, i)).useCoin(i, 1)))) {
                result[i]++;
                return true;
            }
        return false;
    }
}

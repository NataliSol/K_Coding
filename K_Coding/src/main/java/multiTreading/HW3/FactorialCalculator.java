package multiTreading.HW3;

import java.util.concurrent.Callable;

public class FactorialCalculator implements Callable<Long> {
    private int n;

    public FactorialCalculator(int n) {
        this.n = n;
    }

    @Override
    public Long call() throws Exception {
        if (n < 0) {
            throw new IllegalArgumentException("n < 0");
        }
        long result = 1;
        for (int i = 1; i <= n; i++) {
            result = result * i;
        }
        return result;
    }
}
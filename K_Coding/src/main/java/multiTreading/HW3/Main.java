package multiTreading.HW3;

import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        FactorialCalculator calculator = new FactorialCalculator(5);
        Future<Long> future = executor.submit(calculator);
        Long result = future.get();
        System.out.println(result);
        executor.shutdown();
    }
}

package hw28;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Запуск FixedThreadPool 5 потоків");
        testFixedThreadPool();
        Thread.sleep(2000);
        System.out.println("---------------------------------------------");
        testCachedThreadPool();

    }


    public static void testFixedThreadPool() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(5);
        for (int i = 1; i <= 10; i++) {
            int taskNumber = i;
            service.submit(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.println("Завдання #" + taskNumber + " виконує: " + threadName);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        service.shutdown();
        service.awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void testCachedThreadPool() throws InterruptedException {
        ExecutorService cachedExec = Executors.newCachedThreadPool();
        for (int i = 1; i <= 10; i++) {
            int taskNumber = i;
            cachedExec.submit(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.println("Завдання #" + taskNumber + " виконує: " + threadName);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            Thread.sleep(100);
        }
        cachedExec.shutdown();
        cachedExec.awaitTermination(5, TimeUnit.SECONDS);
    }
}

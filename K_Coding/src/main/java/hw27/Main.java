package hw27;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newFixedThreadPool(1);
        System.out.println("Головний потік: Запускаю завдання пошуку...");
        Future<String> future = service.submit(new DataSearcher());
        System.out.println("Головний потік: Пошук триває, я виконую іншу роботу");
        for (int i = 0; i < 3; i++) {
            Thread.sleep(500);
            System.out.println("Головний потік: виконує роботу");
        }
        String result = future.get();
        System.out.println("Головний потік: Отримано результат -> " + result);
        service.shutdown();
    }
}


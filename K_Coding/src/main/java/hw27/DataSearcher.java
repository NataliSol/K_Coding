package hw27;

import java.util.concurrent.Callable;

public class DataSearcher implements Callable<String> {
    @Override
    public String call() throws Exception {
        Thread.sleep(2000);
        return "Дані знайдено!";
    }
}

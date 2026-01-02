package hw26;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public  class Main{
    public static void main(String[] args) {
        ExecutorService service= Executors.newFixedThreadPool(3);
        System.out.println("магазин відкрився");
        for (int i = 0; i <10 ; i++) {
            Order order=new Order(i);
            service.submit(order);
        }
        service.shutdown();
    }
}

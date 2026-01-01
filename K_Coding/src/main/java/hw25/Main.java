package hw25;

public class Main {
    public static void main(String[] args) {
        Stock sharedStock = new Stock();
        Stock.Producer producer = new Stock.Producer(sharedStock);
        Stock.Consumer consumer = new Stock.Consumer(sharedStock);

        Thread producerThread = new Thread(producer);
        Thread consumerThread = new Thread(consumer);

        producerThread.start();
        consumerThread.start();
    }
}
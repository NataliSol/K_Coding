package hw26;


public class Order implements Runnable {
    private final int orderId;

    public Order(int orderId) {
        this.orderId = orderId;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println(threadName + ": Обробка замовлення № " + orderId + " розпочата.");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.err.println("потік було перервано під час обробки замовлення " + orderId);
            throw new RuntimeException(e);
        }
        System.out.println(threadName + " замовлення " + orderId + " оброблено");
    }
}

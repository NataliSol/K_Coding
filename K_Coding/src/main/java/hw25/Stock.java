package hw25;

public class Stock {
    private volatile String item;
    private volatile boolean isNewItemAvaliable = false;

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public boolean isNewItemAvailable() {
        return isNewItemAvaliable;
    }

    public void setNewItemAvailable(boolean isNewItemAvailable) {
        this.isNewItemAvaliable = isNewItemAvailable;
    }

    static class Producer implements Runnable {
        private final Stock stock;

        Producer(Stock stock) {
            this.stock = stock;
        }

        @Override
        public void run() {
            for (int i = 1; i <= 5; i++) {
                synchronized (stock) {
                    try {
                        String newItem = "Товар#" + i;
                        stock.setItem(newItem);
                        stock.setNewItemAvailable(true);

                        System.out.println("Producer: Згенерував -> " + newItem);
                        stock.notify();
                        while (stock.isNewItemAvaliable){
                            stock.wait();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            System.out.println("Producer: Роботу завершено.");
        }
    }
    static class Consumer implements Runnable {
        private final Stock stock;

        public Consumer(Stock stock) {
            this.stock = stock;
        }

        @Override
        public void run() {
            for (int i = 1; i <= 5; i++) {
                synchronized (stock) {
                    try {
                        while (!stock.isNewItemAvailable()) {
                            stock.wait();
                        }

                        String item = stock.getItem();
                        System.out.println("Consumer: Обробив    -> " + item);

                        stock.setNewItemAvailable(false);
                        stock.notify();

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            System.out.println("Consumer: Роботу завершено.");
        }
    }
}


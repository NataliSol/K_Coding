package multiTreading.HW2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class WriterRunnable implements Runnable {

    private final BlockingQueue<Record> outQ;

    public WriterRunnable(BlockingQueue<Record> outQ, AtomicLong written) {
        this.outQ = outQ;
    }

    @Override
    public void run() {
        System.out.println("Writer started");

        try {
            while (true) {
                Record record = outQ.take(); // ждём результат

                System.out.println(
                        record.path + ";" +
                                record.sizeBytes + ";" +
                                record.sha256 + ";" +
                                record.threadName + ";" +
                                record.timeMs
                );
            }
        } catch (InterruptedException e) {
            System.out.println("Writer interrupted");
            Thread.currentThread().interrupt();
        }

        System.out.println("Writer finished");
    }
}

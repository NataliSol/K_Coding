package multiTreading.HW2;

import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    public static void main(String[] args) {
        Path startDir = Path.of("data");
        int queueSize = 256;
        int hasherCount = 2;

        Path POISON = Path.of("__POISON__");

        BlockingQueue<Path> filesQ = new ArrayBlockingQueue<>(queueSize);
        BlockingQueue<Record> outQ  = new ArrayBlockingQueue<>(queueSize);

        AtomicLong scanned = new AtomicLong(0);
        AtomicLong hashed  = new AtomicLong(0);
        AtomicLong written = new AtomicLong(0);

        Thread writer = new Thread(new WriterRunnable(outQ, written), "writer");

        Thread hasher1 = new Thread(new HasherRunnable(filesQ, outQ, POISON, hashed), "hasher-1");
        Thread hasher2 = new Thread(new HasherRunnable(filesQ, outQ, POISON, hashed), "hasher-2");

        Thread scanner = new Thread(new ScannerRunnable(startDir, filesQ, POISON, hasherCount, scanned), "scanner");

        writer.start();
        hasher1.start();
        hasher2.start();
        scanner.start();
    }
}
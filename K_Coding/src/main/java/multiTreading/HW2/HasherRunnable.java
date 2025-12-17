package multiTreading.HW2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class HasherRunnable implements Runnable {
    private final BlockingQueue<Path> filesQ;
    private final BlockingQueue<Record> outQ;
    private final Path poison;

    public HasherRunnable(BlockingQueue<Path> filesQ, BlockingQueue<Record> outQ, Path poison, AtomicLong hashed) {
        this.filesQ = filesQ;
        this.outQ = outQ;
        this.poison = poison;

    }

    @Override
    public void run() {
        while (true) {
            try {
                Path file = filesQ.take();
                if (file.equals(poison)) {
                    break;
                }
    long start = System.nanoTime();
    String hash = sha256(file);
    long timeMs = (System.nanoTime() - start) / 1_000_000;

    Record record = new Record(file.toString(), Files.size(file), hash,
            Thread.currentThread().getName(), timeMs);
                outQ.put(record);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
            }
        }
    }

    private String sha256(Path file) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        try (InputStream in = Files.newInputStream(file)) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                md.update(buffer, 0, read);

            }
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }
}

package multiTreading.HW2;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class ScannerRunnable implements Runnable {

    private final Path startDir;
    private final BlockingQueue<Path> filesQ;
    private final Path poison;
    private final int hasherCount;

    public ScannerRunnable(Path startDir,
                           BlockingQueue<Path> filesQ,
                           Path poison,
                           int hasherCount, AtomicLong scanned) {
        this.startDir = startDir;
        this.filesQ = filesQ;
        this.poison = poison;
        this.hasherCount = hasherCount;
    }

    @Override
    public void run() {
        System.out.println("Scanner started");

        try {
            Files.walkFileTree(startDir, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        filesQ.put(file);
                        System.out.println("Scanner put: " + file);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.out.println("Scanner error: " + e.getMessage());
        }
        for (int i = 0; i < hasherCount; i++) {
            try {
                filesQ.put(poison);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("Scanner finished");
    }
}
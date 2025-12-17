package multiTreading.HW2;


public class Record {

    public final String path;
    public final long sizeBytes;
    public final String sha256;
    public final String threadName;
    public final long timeMs;

    public Record(String path, long sizeBytes, String sha256, String threadName, long timeMs) {
        this.path = path;
            this.sizeBytes = sizeBytes;
            this.sha256 = sha256;
            this.threadName = threadName;
            this.timeMs = timeMs;
        }
}



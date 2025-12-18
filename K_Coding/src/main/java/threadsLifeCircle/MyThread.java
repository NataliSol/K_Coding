package threadsLifeCircle;

public class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("I run");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}

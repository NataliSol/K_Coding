package flowManagementTask;

public class MyThread implements Runnable {
    String name;

    public MyThread(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            System.out.println("потік MyThread працює");
        }
    }
}

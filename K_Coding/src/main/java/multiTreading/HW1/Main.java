package multiTreading.HW1;

public class Main {
    public static void main(String[] args) {
        FrThread first = new FrThread();
        MyRunnable my= new MyRunnable();
        Thread second= new Thread(my);
        second.setDaemon(true);
        second.start();
        first.start();
    }
}

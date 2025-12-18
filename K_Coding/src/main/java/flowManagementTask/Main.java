package flowManagementTask;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        MyThread myThread=new MyThread("Thread-1");
        Thread thread = new Thread(myThread);
        thread.start();
        thread.join();
        System.out.println("Головний потік чекає, дочірній потік завершив роботу");
    }
}

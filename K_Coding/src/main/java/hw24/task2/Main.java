package hw24.task2;

public class Main {
    public static void main(String[] args) {

        Account accountA = new Account(1, 1000);
        Account accountB = new Account(2, 1000);

        Bank bank = new Bank();

        Thread t1 = new Thread(() -> bank.transfer(accountA, accountB, 100));
        Thread t2 = new Thread(() -> bank.transfer(accountB, accountA, 200));

        t1.start();
        t2.start();
    }
}

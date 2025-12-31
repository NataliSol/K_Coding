package hw24.task2;

public class Account {
    private final int id;
    private int balance;

    public Account(int id, int balance) {
        this.id = id;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public synchronized int getBalance() {
        return balance;
    }

    public synchronized void deposit(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("сума повинна бути  > 0");
        balance += amount;
    }

    public synchronized void withdraw(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("сума повинна бути  > 0");
        if (balance < amount) throw new IllegalStateException("не достатьньо грошей");
        balance -= amount;
    }
}

package hw24.task2;
public class Bank {

    public void transfer(Account fromAccount, Account toAccount, int amount) {

        Account first = fromAccount;
        Account second = toAccount;

        if (first.getId() > second.getId()) {
            first = toAccount;
            second = fromAccount;
        }

        synchronized (first) {
            synchronized (second) {
                fromAccount.withdraw(amount);
                toAccount.deposit(amount);
            }
        }
    }
}

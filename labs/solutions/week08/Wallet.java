public class Wallet {
    private long balance;

    Wallet(long balance) {
        this.balance = balance;
    }

    void pay(long amount) {
        if (amount > balance) {
            throw new InsufficientFundsException(amount - balance);
        }
        balance -= amount;
    }

    long getBalance() {
        return balance;
    }
}

// 잔액보다 큰 금액을 pay 하면 InsufficientFundsException(부족한 금액) 을 던진다.
// 예외가 났다면 잔액은 그대로여야 한다.
public class Wallet {
    private long balance;

    Wallet(long balance) {
        this.balance = balance;
    }

    void pay(long amount) {
        // 여기를 고치세요
        balance -= amount;
    }

    long getBalance() {
        return balance;
    }
}

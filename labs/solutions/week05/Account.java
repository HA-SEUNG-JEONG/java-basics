public class Account {
    private static int count = 0;

    private final String owner;
    private final int number;
    private long balance;

    Account(String owner, long initial) {
        this.owner = owner;
        this.balance = initial;
        this.number = ++count;
    }

    void deposit(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("입금액은 0보다 커야 합니다");
        }
        balance += amount;
    }

    boolean withdraw(long amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }

    boolean transfer(Account to, long amount) {
        if (!withdraw(amount)) {
            return false;
        }
        to.deposit(amount);
        return true;
    }

    long getBalance() { return balance; }

    String getOwner() { return owner; }

    int getNumber() { return number; }

    static int totalAccounts() { return count; }

    @Override
    public String toString() {
        return getOwner() + "의 잔액: " + getBalance() + "원";
    }
}

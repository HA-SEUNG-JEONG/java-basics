// 5주차 실습 1: 계좌 클래스를 완성하세요. 채점:  cd labs && ./check.sh 05
//
// 규칙
//  - 필드는 전부 private (static 포함)
//  - 계좌번호는 1부터 만든 순서대로 자동 부여 (static 카운터)
//  - deposit: 0 이하 금액이면 IllegalArgumentException
//  - withdraw: 0 이하이거나 잔액보다 크면 false, 성공하면 true
//  - transfer: 출금에 실패하면 입금도 하지 않고 false
public class Account {
    // 여기에 필드를 선언하세요 (owner, balance, number, 그리고 static 카운터)

    Account(String owner, long initial) {
        // 여기를 고치세요
    }

    void deposit(long amount) {
        // 여기를 고치세요
    }

    boolean withdraw(long amount) {
        return false; // 여기를 고치세요
    }

    boolean transfer(Account to, long amount) {
        return false; // 여기를 고치세요
    }

    long getBalance() {
        return 0; // 여기를 고치세요
    }

    String getOwner() {
        return ""; // 여기를 고치세요
    }

    int getNumber() {
        return 0; // 여기를 고치세요
    }

    /** 지금까지 만들어진 계좌의 총 개수 */
    static int totalAccounts() {
        return 0; // 여기를 고치세요
    }

    @Override
    public String toString() {
        return getOwner() + "의 잔액: " + getBalance() + "원";
    }
}

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class Check {
    public static void main(String[] args) {
        int before = Account.totalAccounts();
        Account a = new Account("하승", 10_000);
        Account b = new Account("민지", 0);

        T.eq("getOwner / getBalance 초기값", "하승:10000", () -> a.getOwner() + ":" + a.getBalance());
        T.eq("계좌번호는 만든 순서대로 1씩 증가", true, () -> b.getNumber() == a.getNumber() + 1 && a.getNumber() > 0);
        T.eq("totalAccounts() 는 만든 개수만큼 늘어난다", before + 2, () -> Account.totalAccounts());

        a.deposit(5_000);
        T.eq("deposit(5000) 후 잔액 15000", 15_000L, () -> a.getBalance());
        T.thrown("deposit(0) 은 IllegalArgumentException", IllegalArgumentException.class, () -> a.deposit(0));
        T.thrown("deposit(-1) 은 IllegalArgumentException", IllegalArgumentException.class, () -> a.deposit(-1));

        T.eq("withdraw(20000) 잔액 부족이면 false", false, () -> a.withdraw(20_000));
        T.eq("withdraw(3000) 성공이면 true", true, () -> a.withdraw(3_000));
        T.eq("withdraw 성공 후 잔액 12000", 12_000L, () -> a.getBalance());
        T.eq("withdraw(0) 은 false", false, () -> a.withdraw(0));

        T.eq("transfer(b, 2000) 성공", true, () -> a.transfer(b, 2_000));
        T.eq("transfer 후 잔액 a=10000 b=2000", "10000:2000", () -> a.getBalance() + ":" + b.getBalance());
        T.eq("transfer 실패하면 false", false, () -> a.transfer(b, 999_999));
        T.eq("transfer 실패하면 b 의 잔액도 그대로 (입금되면 안 된다)", 2_000L, () -> b.getBalance());

        T.ok("모든 필드가 private 인가 (캡슐화)", () -> {
            Field[] fields = Account.class.getDeclaredFields();
            return fields.length >= 4 && Arrays.stream(fields).allMatch(f -> Modifier.isPrivate(f.getModifiers()));
        }, "owner, balance, number, static 카운터까지 최소 4개 필드를 모두 private 로 선언하세요");

        IntStack s = new IntStack(2);
        T.eq("새 스택은 비어 있다", true, () -> s.isEmpty() && s.size() == 0);
        s.push(10);
        s.push(20);
        T.eq("push 두 번 후 size=2, peek=20", "2:20", () -> s.size() + ":" + s.peek());
        T.thrown("용량(2) 초과 push 는 IllegalStateException", IllegalStateException.class, () -> s.push(30));
        T.eq("pop 은 나중에 넣은 것부터 (LIFO)", "20:10", () -> s.pop() + ":" + s.pop());
        T.thrown("빈 스택 pop 은 IllegalStateException", IllegalStateException.class, () -> s.pop());
        T.thrown("빈 스택 peek 도 IllegalStateException", IllegalStateException.class, () -> s.peek());
        T.done();
    }
}

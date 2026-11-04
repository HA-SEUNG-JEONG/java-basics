import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Check {
    public static void main(String[] args) {
        InsufficientFundsException ex = new InsufficientFundsException(500);
        T.eq("예외 메시지", "잔액이 500원 부족합니다", () -> ex.getMessage());
        T.eq("getShortage() = 500", 500L, () -> ex.getShortage());
        T.ok("RuntimeException 이다 (unchecked)", () -> ex instanceof RuntimeException, "");

        Wallet w = new Wallet(1000);
        T.thrown("잔액 초과 pay 는 InsufficientFundsException", InsufficientFundsException.class, () -> w.pay(1500));
        T.eq("예외가 났다면 잔액은 그대로 1000", 1000L, () -> w.getBalance());
        T.ok("부족 금액이 500 으로 담겨 있다", () -> {
            try {
                w.pay(1500);
                return false;
            } catch (InsufficientFundsException e) {
                return e.getShortage() == 500;
            }
        }, "new InsufficientFundsException(amount - balance)");
        w.pay(300);
        T.eq("정상 pay(300) 후 잔액 700", 700L, () -> w.getBalance());

        T.eq("toIntOrDefault(\"42\", -1) = 42", 42, () -> Parse.toIntOrDefault("42", -1));
        T.eq("toIntOrDefault(\"abc\", -1) = -1", -1, () -> Parse.toIntOrDefault("abc", -1));
        T.eq("toIntOrDefault(null, 7) = 7", 7, () -> Parse.toIntOrDefault(null, 7));
        T.eq("safeDivide(10, 2) = \"5\"", "5", () -> Parse.safeDivide(10, 2));
        T.eq("safeDivide(1, 0) = \"ERR\"", "ERR", () -> Parse.safeDivide(1, 0));

        Product a = new Product(1, "노트북");
        Product b = new Product(1, "노트북");
        Product c = new Product(2, "노트북");
        T.eq("내용이 같으면 equals true", true, () -> a.equals(b));
        T.eq("id 가 다르면 equals false", false, () -> a.equals(c));
        T.eq("equals(null) 은 false", false, () -> a.equals(null));
        T.eq("다른 타입과는 false", false, () -> a.equals("노트북"));
        T.eq("같은 객체끼리는 hashCode 가 같다", true, () -> a.hashCode() == b.hashCode());
        T.eq("HashSet 에 넣으면 중복이 제거된다", 2, () -> {
            Set<Product> set = new HashSet<>(List.of(a, c));
            set.add(b);
            return set.size();
        });

        T.eq("run(false)", List.of("open A", "open B", "close B", "close A"), () -> Resources.run(false));
        T.eq("run(true)  예외가 나도 close 는 역순으로 실행된다",
                List.of("open A", "open B", "close B", "close A", "caught"), () -> Resources.run(true));
        String src = T.source(args[0] + "/Resources.java").replaceAll("(?s)/\\*.*?\\*/", "");
        T.ok("try-with-resources 문법을 썼는가", () -> src.contains("try ("), "try (Res a = new Res(\"A\"); ...) { ... }");
        T.done();
    }
}

public class Check {
    public static void main(String[] args) {
        T.eq("PercentDiscount(10).apply(20000) = 18000", 18000, () -> new PercentDiscount(10).apply(20000));
        T.eq("PercentDiscount(0).apply(500) = 500", 500, () -> new PercentDiscount(0).apply(500));
        T.eq("FixedDiscount(3000).apply(10000) = 7000", 7000, () -> new FixedDiscount(3000).apply(10000));
        T.eq("FixedDiscount(3000).apply(2000) = 0  (음수 가격 금지)", 0, () -> new FixedDiscount(3000).apply(2000));

        int[] prices = {10000, 20000, 30000};
        T.eq("Cart.total(10% 할인) = 54000", 54000, () -> Cart.total(prices, new PercentDiscount(10)));
        T.eq("Cart.total(정액 5000 할인) = 45000", 45000, () -> Cart.total(prices, new FixedDiscount(5000)));
        T.eq("Cart.total(람다 p -> p / 2) = 30000", 30000, () -> Cart.total(prices, p -> p / 2));
        String src = T.source(args[0] + "/Cart.java").replaceAll("//.*", "");
        T.ok("Cart 가 구체 클래스 이름을 모르는가 (인터페이스에만 의존)",
                () -> !src.contains("PercentDiscount") && !src.contains("FixedDiscount"),
                "Cart 안에서 Discount 인터페이스의 apply 만 호출하세요");

        T.eq("MON.isWeekend() = false", false, () -> Day.MON.isWeekend());
        T.eq("SAT, SUN 은 주말", true, () -> Day.SAT.isWeekend() && Day.SUN.isWeekend());
        T.eq("FRI.isWeekend() = false", false, () -> Day.FRI.isWeekend());
        T.eq("MON.next() = TUE", Day.TUE, () -> Day.MON.next());
        T.eq("SUN.next() = MON  (한 바퀴)", Day.MON, () -> Day.SUN.next());

        T.eq("Money.add (같은 통화)", new Money(1500, "KRW"), () -> new Money(1000, "KRW").add(new Money(500, "KRW")));
        T.thrown("Money.add (다른 통화) 는 IllegalArgumentException", IllegalArgumentException.class,
                () -> new Money(1000, "KRW").add(new Money(1, "USD")));
        T.thrown("new Money(-1, \"KRW\") 는 IllegalArgumentException", IllegalArgumentException.class,
                () -> new Money(-1, "KRW"));
        T.eq("record 는 equals 가 내용 비교", true, () -> new Money(5, "KRW").equals(new Money(5, "KRW")));

        T.eq("eval(Num(5)) = 5", 5, () -> Eval.eval(new Num(5)));
        T.eq("eval(Add(Num(1), Mul(Num(2), Num(3)))) = 7", 7,
                () -> Eval.eval(new Add(new Num(1), new Mul(new Num(2), new Num(3)))));
        T.eq("eval(Mul(Add(Num(1), Num(2)), Add(Num(3), Num(4)))) = 21", 21,
                () -> Eval.eval(new Mul(new Add(new Num(1), new Num(2)), new Add(new Num(3), new Num(4)))));
        String src2 = T.source(args[0] + "/Eval.java").replaceAll("(?s)/\\*.*?\\*/", "");
        T.ok("Eval 에 default 분기가 없는가 (sealed 가 완전성을 보장)", () -> !src2.contains("default"),
                "default 를 지워도 컴파일되는지 확인해 보세요");
        T.done();
    }
}

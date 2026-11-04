/** 금액을 받아 "깎아 줄 금액"을 돌려준다. 인터페이스이므로 람다로도 만들 수 있다. */
@FunctionalInterface
public interface DiscountPolicy {
    int discount(int amount);
}

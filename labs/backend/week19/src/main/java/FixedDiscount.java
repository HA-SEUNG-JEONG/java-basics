/** 정해진 금액을 깎아 준다. 단, 주문 금액보다 많이 깎을 수는 없다 (최종 금액은 0 아래로 내려가지 않는다). */
public class FixedDiscount implements DiscountPolicy {
    private final int won;

    public FixedDiscount(int won) {
        this.won = won;
    }

    @Override
    public int discount(int amount) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("FixedDiscount 를 구현하세요");
    }
}

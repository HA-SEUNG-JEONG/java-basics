/** 금액의 percent% 를 깎아 준다. 소수점 이하는 버린다. 예: 15500원의 10% = 1550원, 15505원의 10% = 1550원 */
public class PercentDiscount implements DiscountPolicy {
    private final int percent;

    public PercentDiscount(int percent) {
        this.percent = percent;
    }

    @Override
    public int discount(int amount) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("PercentDiscount 를 구현하세요");
    }
}

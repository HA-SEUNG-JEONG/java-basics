public class PercentDiscount implements DiscountPolicy {
    private final int percent;

    public PercentDiscount(int percent) {
        this.percent = percent;
    }

    @Override
    public int discount(int amount) {
        return amount * percent / 100;
    }
}

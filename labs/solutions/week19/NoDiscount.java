public class NoDiscount implements DiscountPolicy {
    @Override
    public int discount(int amount) {
        return 0;
    }
}

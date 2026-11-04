public class FixedDiscount implements DiscountPolicy {
    private final int won;

    public FixedDiscount(int won) {
        this.won = won;
    }

    @Override
    public int discount(int amount) {
        return Math.min(won, amount);
    }
}

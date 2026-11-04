public class FixedDiscount implements Discount {
    private final int amount;

    FixedDiscount(int amount) {
        this.amount = amount;
    }

    @Override
    public int apply(int price) {
        return Math.max(0, price - amount);
    }
}

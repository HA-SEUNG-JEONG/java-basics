public class PercentDiscount implements Discount {
    private final int percent;

    PercentDiscount(int percent) {
        this.percent = percent;
    }

    @Override
    public int apply(int price) {
        return price * (100 - percent) / 100;
    }
}

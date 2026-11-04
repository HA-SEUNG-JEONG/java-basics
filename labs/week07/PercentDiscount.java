// percent% 할인. 정수로 계산한다: price * (100 - percent) / 100.  apply(20000) with 10 -> 18000
public class PercentDiscount implements Discount {
    PercentDiscount(int percent) {
        // 여기를 고치세요
    }

    @Override
    public int apply(int price) {
        return price; // 여기를 고치세요
    }
}

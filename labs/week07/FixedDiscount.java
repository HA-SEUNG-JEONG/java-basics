// 정액 할인. price - amount 이지만 0 아래로는 내려가지 않는다
public class FixedDiscount implements Discount {
    FixedDiscount(int amount) {
        // 여기를 고치세요
    }

    @Override
    public int apply(int price) {
        return price; // 여기를 고치세요
    }
}

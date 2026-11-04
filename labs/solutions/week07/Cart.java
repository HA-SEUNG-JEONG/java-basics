public class Cart {
    static int total(int[] prices, Discount discount) {
        int sum = 0;
        for (int p : prices) sum += discount.apply(p);
        return sum;
    }
}

public class OrderService {
    private final DiscountPolicy policy;
    private final OrderRepository repository;
    private final Notifier notifier;

    public OrderService(DiscountPolicy policy, OrderRepository repository, Notifier notifier) {
        this.policy = policy;
        this.repository = repository;
        this.notifier = notifier;
    }

    public Order placeOrder(String customer, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("금액은 0보다 커야 합니다: " + amount);
        int discount = policy.discount(amount);
        Order order = new Order(customer, amount, discount, amount - discount);
        repository.save(order);
        notifier.send(customer + "님 주문 완료: " + order.finalAmount() + "원");
        return order;
    }
}

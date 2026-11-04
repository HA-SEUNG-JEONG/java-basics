import java.util.ArrayList;
import java.util.List;

/** 실제 서비스에서는 DB 구현으로 바뀔 자리. 이 파일은 고치지 않습니다. */
public class InMemoryOrderRepository implements OrderRepository {
    private final List<Order> orders = new ArrayList<>();

    @Override public void save(Order order) { orders.add(order); }
    @Override public List<Order> findAll() { return List.copyOf(orders); }
}

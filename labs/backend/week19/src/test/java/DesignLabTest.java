import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesignLabTest {
    // 테스트 전용 가짜 객체: 진짜 DB 도 메일 서버도 필요 없다는 것이 DI 의 이점입니다.
    static class FakeNotifier implements Notifier {
        final List<String> sent = new ArrayList<>();
        @Override public void send(String message) { sent.add(message); }
    }

    @Test
    void percentDiscount_roundsDown() {
        assertEquals(1550, new PercentDiscount(10).discount(15500));
        assertEquals(1550, new PercentDiscount(10).discount(15505));
        assertEquals(0, new PercentDiscount(10).discount(5));
    }

    @Test
    void fixedDiscount_neverExceedsAmount() {
        assertEquals(3000, new FixedDiscount(3000).discount(10000));
        assertEquals(2000, new FixedDiscount(3000).discount(2000));
    }

    @Test
    void noDiscount_isZero() {
        assertEquals(0, new NoDiscount().discount(12345));
    }

    @Test
    void factory_picksPolicyByGrade() {
        assertEquals(2000, DiscountPolicies.forGrade("VIP").discount(10000));
        assertEquals(1000, DiscountPolicies.forGrade("GOLD").discount(10000));
        assertEquals(3000, DiscountPolicies.forGrade("NEW").discount(10000));
        assertEquals(0, DiscountPolicies.forGrade("NORMAL").discount(10000));
    }

    @Test
    void factory_rejectsUnknownGrade() {
        var e = assertThrows(IllegalArgumentException.class, () -> DiscountPolicies.forGrade("PLATINUM"));
        assertTrue(e.getMessage().contains("PLATINUM"));
        assertThrows(IllegalArgumentException.class, () -> DiscountPolicies.forGrade(null));
    }

    @Test
    void orderService_savesAndNotifies() {
        var repo = new InMemoryOrderRepository();
        var notifier = new FakeNotifier();
        var service = new OrderService(new PercentDiscount(10), repo, notifier);

        Order order = service.placeOrder("하승", 20000);

        assertEquals(new Order("하승", 20000, 2000, 18000), order);
        assertEquals(List.of(order), repo.findAll());
        assertEquals(List.of("하승님 주문 완료: 18000원"), notifier.sent);
    }

    @Test
    void orderService_rejectsNonPositiveAmount_withoutSideEffects() {
        var repo = new InMemoryOrderRepository();
        var notifier = new FakeNotifier();
        var service = new OrderService(new NoDiscount(), repo, notifier);

        assertThrows(IllegalArgumentException.class, () -> service.placeOrder("하승", 0));
        assertTrue(repo.findAll().isEmpty());
        assertTrue(notifier.sent.isEmpty());
    }

    @Test
    void newPolicyNeedsNoChangeInOrderService() {
        // "블랙프라이데이: 무조건 1000원 할인" 이라는 새 정책을 테스트 안에서 람다로 만들어 끼워 본다.
        var service = new OrderService(amount -> 1000, new InMemoryOrderRepository(), new FakeNotifier());
        assertEquals(4000, service.placeOrder("민지", 5000).finalAmount());
    }

    @Test
    void orderService_doesNotNameConcreteClasses() throws IOException {
        String src = Files.readString(Path.of("src/main/java/OrderService.java"));
        // 주석을 뺀 코드 줄만 검사한다
        String code = src.lines().filter(l -> !l.trim().startsWith("*") && !l.trim().startsWith("/*")
                && !l.trim().startsWith("//")).reduce("", (a, b) -> a + b + "\n");
        for (String concrete : List.of("PercentDiscount", "FixedDiscount", "NoDiscount",
                "InMemoryOrderRepository", "DiscountPolicies")) {
            assertFalse(code.contains(concrete),
                    "OrderService 가 구체 클래스 " + concrete + " 를 직접 알고 있습니다. 인터페이스 타입으로만 받으세요");
        }
    }

    @Test
    void eventBus_deliversInOrder() {
        var bus = new EventBus<String>();
        List<String> log = new ArrayList<>();
        bus.subscribe(e -> log.add("A:" + e));
        bus.subscribe(e -> log.add("B:" + e));
        bus.publish("x");
        bus.publish("y");
        assertEquals(List.of("A:x", "B:x", "A:y", "B:y"), log);
    }

    @Test
    void eventBus_oneFailingListenerDoesNotBlockOthers() {
        var bus = new EventBus<Integer>();
        List<Integer> got = new ArrayList<>();
        bus.subscribe(e -> { throw new IllegalStateException("구독자 고장"); });
        bus.subscribe(got::add);
        assertDoesNotThrow(() -> bus.publish(7));
        assertEquals(List.of(7), got);
    }

    @Test
    void eventBus_publishWithNoListenersIsFine() {
        assertDoesNotThrow(() -> new EventBus<String>().publish("아무도 안 듣는다"));
    }
}

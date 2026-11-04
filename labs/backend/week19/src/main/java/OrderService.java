/**
 * 19주차 실습의 중심. 의존 대상 3개를 "생성자로 받습니다" (new 로 직접 만들지 않습니다).
 * placeOrder 규칙
 *   1) amount 가 0 이하면 IllegalArgumentException
 *   2) 할인액 = policy.discount(amount),  최종 금액 = amount - 할인액
 *   3) Order 를 repository 에 저장
 *   4) notifier 로 "{customer}님 주문 완료: {최종금액}원" 전송
 *   5) 저장한 Order 를 돌려준다
 * 이 파일 안에는 PercentDiscount, InMemoryOrderRepository 같은 구체 클래스 이름이 나오면 안 됩니다. (테스트가 검사합니다)
 */
public class OrderService {
    // 여기를 고치세요: 받은 3개를 필드에 보관하세요 (필드 선언도 직접 추가)
    public OrderService(DiscountPolicy policy, OrderRepository repository, Notifier notifier) {
    }

    public Order placeOrder(String customer, int amount) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("placeOrder 를 구현하세요");
    }
}

import java.util.function.Consumer;

/**
 * 옵저버 패턴. subscribe 로 등록한 구독자 전원에게 publish 한 이벤트를 "등록한 순서대로" 전달한다.
 * 한 구독자가 예외를 던져도 다음 구독자에게는 계속 전달해야 한다.
 */
public class EventBus<E> {
    // 여기를 고치세요: 구독자 목록 필드

    public void subscribe(Consumer<E> listener) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("subscribe 를 구현하세요");
    }

    public void publish(E event) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("publish 를 구현하세요");
    }
}

package shop;

import java.util.Map;

/**
 * 해야 할 일
 *   - 이 클래스를 웹 요청을 받는 컨트롤러로 표시 (응답 본문이 곧 문자열/JSON)
 *   - 생성자로 의존 객체를 받기: 기본 Greeter, "englishGreeter" 라는 이름의 Greeter, VisitCounter
 *   - GET /greet?name=하승        -> 방문 횟수를 1 올리고 기본 Greeter 로 인사 (문자열)
 *   - GET /greet/en?name=Ha       -> 방문 횟수를 1 올리고 영어 Greeter 로 인사 (문자열)
 *   - GET /visits                 -> {"count": 현재 방문 횟수}  (JSON)
 * name 파라미터가 없으면 "손님" 을 씁니다.
 */
public class GreetingController {
    public GreetingController(Greeter greeter, Greeter english, VisitCounter counter) {
        // 여기를 고치세요: 받은 값을 필드에 보관 (필드 선언도 직접 추가)
    }

    public String greet(String name) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("greet 를 구현하세요");
    }

    public String greetEnglish(String name) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("greetEnglish 를 구현하세요");
    }

    public Map<String, Integer> visits() {
        // 여기를 고치세요
        throw new UnsupportedOperationException("visits 를 구현하세요");
    }
}

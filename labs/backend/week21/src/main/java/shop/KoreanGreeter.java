package shop;

/**
 * 21주차 실습. 아직 Spring 이 이 클래스를 모릅니다. (빈이 아닙니다)
 * 해야 할 일
 *   1) Spring 이 객체를 만들어 관리하도록 표시하기
 *   2) 같은 Greeter 가 둘(Korean, English)이라 "어느 쪽이 기본이냐" 를 정해 주기
 *   3) application.yml 의 app.greeting 값을 greeting 으로 받기 (값이 없으면 "안녕")
 * 결과: greet("하승") -> "안녕, 하승!"
 */
public class KoreanGreeter implements Greeter {
    private final String greeting;

    public KoreanGreeter(String greeting) {
        this.greeting = greeting;
    }

    @Override
    public String greet(String name) {
        return greeting + ", " + name + "!";
    }
}

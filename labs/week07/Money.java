// record 는 생성자, getter, equals, hashCode, toString 이 자동이다. 규칙만 추가하자.
//  - amount 가 음수면 IllegalArgumentException (컴팩트 생성자에서 검사. public record 라서 생성자도 public 이어야 한다)
//  - add: 통화가 같으면 합친 새 Money, 다르면 IllegalArgumentException
public record Money(long amount, String currency) {
    Money add(Money other) {
        return this; // 여기를 고치세요
    }
}

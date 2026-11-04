/**
 * 팩토리: 등급 문자열 -> 정책 객체. "어떤 정책을 쓸지 고르는 if/switch" 는 이 한 곳에만 둡니다.
 *   VIP -> 20%,  GOLD -> 10%,  NEW -> 3000원 정액,  NORMAL -> 할인 없음
 *   null 이나 모르는 등급 -> IllegalArgumentException("알 수 없는 등급: ...")
 * 힌트: switch 표현식
 */
public final class DiscountPolicies {
    private DiscountPolicies() {}

    public static DiscountPolicy forGrade(String grade) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("forGrade 를 구현하세요");
    }
}

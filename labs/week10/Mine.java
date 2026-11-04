import java.time.LocalDate;
import java.time.LocalDateTime;

// 10주차 실습. 채점:  cd labs && ./check.sh 10
public class Mine {
    /** 대소문자를 무시한 회문. "Level" -> true, "abc" -> false, "" -> true */
    static boolean isPalindrome(String s) {
        return false; // 여기를 고치세요
    }

    /** 단어 개수 (공백이 여러 개여도, 앞뒤에 있어도 된다). "  hello  java world " -> 3, "" -> 0 */
    static int wordCount(String s) {
        return -1; // 여기를 고치세요
    }

    /** 가장 긴 단어 (같으면 먼저 나온 것). "I love programming" -> "programming" */
    static String longestWord(String s) {
        return ""; // 여기를 고치세요
    }

    /** "user@example.com" -> "example.com".  '@' 가 없으면 IllegalArgumentException */
    static String emailDomain(String email) {
        return ""; // 여기를 고치세요
    }

    /** 1부터 n까지 쉼표로 이은 문자열 "1,2,3" (StringBuilder 사용, 마지막에 쉼표 없음) */
    static String countUp(int n) {
        return ""; // 여기를 고치세요
    }

    /** 두 날짜 사이의 일수 */
    static long daysUntil(LocalDate from, LocalDate to) {
        return -1; // 여기를 고치세요
    }

    /** 만 나이: 생일이 아직 안 지났으면 1살 덜 센다 */
    static int manAge(LocalDate birth, LocalDate today) {
        return -1; // 여기를 고치세요
    }

    /** 다음 생일까지 남은 일수 (오늘이 생일이면 0) */
    static long daysToNextBirthday(LocalDate birth, LocalDate today) {
        return -1; // 여기를 고치세요
    }

    /** "2026년 10월 20일 14:30" 형태 (DateTimeFormatter) */
    static String formatKorean(LocalDateTime dt) {
        return ""; // 여기를 고치세요
    }

    /** 0.1 + 0.2 를 BigDecimal 로 정확히 계산해 "0.3" 같은 문자열로. 입력은 문자열 (toPlainString 사용) */
    static String exactSum(String a, String b) {
        return ""; // 여기를 고치세요
    }
}

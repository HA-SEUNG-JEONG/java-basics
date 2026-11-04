import java.time.LocalDate;
import java.time.LocalDateTime;

public class Check {
    public static void main(String[] args) {
        T.eq("isPalindrome(\"Level\")", true, () -> Mine.isPalindrome("Level"));
        T.eq("isPalindrome(\"abc\")", false, () -> Mine.isPalindrome("abc"));
        T.eq("isPalindrome(\"\")", true, () -> Mine.isPalindrome(""));

        T.eq("wordCount(\"  hello  java world \") = 3", 3, () -> Mine.wordCount("  hello  java world "));
        T.eq("wordCount(\"\") = 0", 0, () -> Mine.wordCount(""));
        T.eq("wordCount(\"one\") = 1", 1, () -> Mine.wordCount("one"));
        T.eq("longestWord", "programming", () -> Mine.longestWord("I love programming"));
        T.eq("longestWord 같으면 먼저 나온 것", "aa", () -> Mine.longestWord("aa bb c"));

        T.eq("emailDomain", "example.com", () -> Mine.emailDomain("user@example.com"));
        T.thrown("emailDomain('@' 없음) 은 IllegalArgumentException", IllegalArgumentException.class,
                () -> Mine.emailDomain("userexample.com"));

        T.eq("countUp(5)", "1,2,3,4,5", () -> Mine.countUp(5));
        T.eq("countUp(1)", "1", () -> Mine.countUp(1));
        T.ok("countUp 은 StringBuilder 로 만들었는가", () -> T.source(args[0] + "/Mine.java").contains("StringBuilder"),
                "반복문에서 문자열을 += 로 이어붙이지 말고 StringBuilder 를 쓰세요");

        LocalDate d = LocalDate.of(2026, 10, 20);
        T.eq("daysUntil(10/20, 12/25) = 66", 66L, () -> Mine.daysUntil(d, LocalDate.of(2026, 12, 25)));
        T.eq("daysUntil 은 음수도 된다", -1L, () -> Mine.daysUntil(d, d.minusDays(1)));

        LocalDate birth = LocalDate.of(1996, 3, 15);
        T.eq("manAge: 생일 지난 뒤(2026-10-20) = 30", 30, () -> Mine.manAge(birth, d));
        T.eq("manAge: 생일 하루 전(2026-03-14) = 29", 29, () -> Mine.manAge(birth, LocalDate.of(2026, 3, 14)));
        T.eq("manAge: 생일 당일(2026-03-15) = 30", 30, () -> Mine.manAge(birth, LocalDate.of(2026, 3, 15)));

        T.eq("daysToNextBirthday: 생일 당일 = 0", 0L, () -> Mine.daysToNextBirthday(birth, LocalDate.of(2026, 3, 15)));
        T.eq("daysToNextBirthday: 생일 다음 날 = 364", 364L, () -> Mine.daysToNextBirthday(birth, LocalDate.of(2026, 3, 16)));
        T.eq("daysToNextBirthday: 2026-10-20 -> 2027-03-15 = 146", 146L, () -> Mine.daysToNextBirthday(birth, d));

        T.eq("formatKorean", "2026년 10월 20일 14:30", () -> Mine.formatKorean(LocalDateTime.of(2026, 10, 20, 14, 30)));
        T.eq("exactSum(\"0.1\", \"0.2\") = \"0.3\"  (double 이면 0.30000000000000004)", "0.3", () -> Mine.exactSum("0.1", "0.2"));
        T.done();
    }
}

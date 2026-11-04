import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Mine {
    static boolean isPalindrome(String s) {
        String lower = s.toLowerCase();
        return new StringBuilder(lower).reverse().toString().equals(lower);
    }

    static int wordCount(String s) {
        String t = s.strip();
        return t.isEmpty() ? 0 : t.split("\\s+").length;
    }

    static String longestWord(String s) {
        String best = "";
        for (String w : s.strip().split("\\s+")) {
            if (w.length() > best.length()) best = w;
        }
        return best;
    }

    static String emailDomain(String email) {
        int at = email.indexOf('@');
        if (at < 0) {
            throw new IllegalArgumentException("@ 가 없습니다: " + email);
        }
        return email.substring(at + 1);
    }

    static String countUp(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= n; i++) {
            sb.append(i).append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    static long daysUntil(LocalDate from, LocalDate to) {
        return ChronoUnit.DAYS.between(from, to);
    }

    static int manAge(LocalDate birth, LocalDate today) {
        return java.time.Period.between(birth, today).getYears();
    }

    static long daysToNextBirthday(LocalDate birth, LocalDate today) {
        LocalDate next = birth.withYear(today.getYear());
        if (next.isBefore(today)) next = next.plusYears(1);
        return ChronoUnit.DAYS.between(today, next);
    }

    static String formatKorean(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm"));
    }

    static String exactSum(String a, String b) {
        return new BigDecimal(a).add(new BigDecimal(b)).toPlainString();
    }
}

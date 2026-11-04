// 2주차 실습: 메서드 본문을 채우세요. main 은 없습니다.
// 채점:  cd labs && ./check.sh 02
public class Mine {
    /** 섭씨 -> 화씨. F = C * 9 / 5 + 32 */
    static double toFahrenheit(double c) {
        return c * 9 / 5 + 32;
    }

    /** 총 초를 "1시간 2분 5초" 형태로. 예: 3725 -> "1시간 2분 5초" */
    static String hms(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = totalSeconds % 3600 / 60;
        int seconds = totalSeconds % 60;
        return hours + "시간 " + minutes + "분 " + seconds + "초";
    }

    /** 세 점수의 평균. 소수점까지 정확해야 한다. (정수 나눗셈 함정!) */
    static double average(int a, int b, int c) {
        return (a + b + c) / 3.0;
    }

    /** 두 int 의 합. 결과가 int 범위를 넘어도 정확해야 한다. */
    static long safeSum(int a, int b) {
        return (long)a + b;
    }

    /** 내용이 같은 문자열이면 true. (== 로는 안 된다) */
    static boolean sameText(String a, String b) {
        return a.equals(b); // 여기를 고치세요
    }

    /** 모음(a e i o u, 소문자) 개수. 예: "education" -> 5 */
    static int vowels(String s) {
        int count = 0;
        for (char ch : s.toCharArray()) {
            if("aeiou".indexOf(ch) != -1) {
                count++;
            }
        }
        return count;

    }
}

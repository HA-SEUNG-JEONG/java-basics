public class Mine {
    static double toFahrenheit(double c) {
        return c * 9 / 5 + 32;
    }

    static String hms(int totalSeconds) {
        int h = totalSeconds / 3600;
        int m = totalSeconds % 3600 / 60;
        int s = totalSeconds % 60;
        return h + "시간 " + m + "분 " + s + "초";
    }

    static double average(int a, int b, int c) {
        return (a + b + c) / 3.0;
    }

    static long safeSum(int a, int b) {
        return (long) a + b;
    }

    static boolean sameText(String a, String b) {
        return a.equals(b);
    }

    static int vowels(String s) {
        return s.length() - s.replaceAll("[aeiou]", "").length();
    }
}

public class Check {
    public static void main(String[] args) {
        T.near("toFahrenheit(100) = 212", 212, () -> Mine.toFahrenheit(100));
        T.near("toFahrenheit(36.5) = 97.7", 97.7, () -> Mine.toFahrenheit(36.5));
        T.near("toFahrenheit(-40) = -40", -40, () -> Mine.toFahrenheit(-40));

        T.eq("hms(3725)", "1시간 2분 5초", () -> Mine.hms(3725));
        T.eq("hms(59)", "0시간 0분 59초", () -> Mine.hms(59));
        T.eq("hms(3600)", "1시간 0분 0초", () -> Mine.hms(3600));

        T.near("average(80, 95, 70)", 81.6666666667, () -> Mine.average(80, 95, 70));
        T.near("average(1, 2, 2) = 1.666..  (정수 나눗셈이면 1.0)", 1.6666666667, () -> Mine.average(1, 2, 2));

        T.eq("safeSum(1, 2)", 3L, () -> Mine.safeSum(1, 2));
        T.eq("safeSum(MAX_VALUE, 1)  (int 오버플로우 함정)", 2147483648L, () -> Mine.safeSum(Integer.MAX_VALUE, 1));

        T.eq("sameText(new String(\"hi\"), new String(\"hi\"))", true,
                () -> Mine.sameText(new String("hi"), new String("hi")));
        T.eq("sameText(\"hi\", \"HI\")", false, () -> Mine.sameText("hi", "HI"));

        T.eq("vowels(\"education\")", 5, () -> Mine.vowels("education"));
        T.eq("vowels(\"rhythm\")", 0, () -> Mine.vowels("rhythm"));
        T.eq("vowels(\"Java is fun\")", 4, () -> Mine.vowels("Java is fun"));
        T.done();
    }
}

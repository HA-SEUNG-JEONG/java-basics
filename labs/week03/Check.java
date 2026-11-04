public class Check {
    public static void main(String[] args) {
        T.eq("sumEven(10) = 30", 30, () -> Mine.sumEven(10));
        T.eq("sumEven(1) = 0", 0, () -> Mine.sumEven(1));
        T.eq("sumEven(100) = 2550", 2550, () -> Mine.sumEven(100));

        T.eq("fizzBuzz(15)", "FizzBuzz", () -> Mine.fizzBuzz(15));
        T.eq("fizzBuzz(9)", "Fizz", () -> Mine.fizzBuzz(9));
        T.eq("fizzBuzz(10)", "Buzz", () -> Mine.fizzBuzz(10));
        T.eq("fizzBuzz(7)", "7", () -> Mine.fizzBuzz(7));

        T.eq("isLeap(2000) 400의 배수", true, () -> Mine.isLeap(2000));
        T.eq("isLeap(1900) 100의 배수", false, () -> Mine.isLeap(1900));
        T.eq("isLeap(2024)", true, () -> Mine.isLeap(2024));
        T.eq("isLeap(2023)", false, () -> Mine.isLeap(2023));

        T.eq("digitSum(1234) = 10", 10, () -> Mine.digitSum(1234));
        T.eq("digitSum(0) = 0", 0, () -> Mine.digitSum(0));
        T.eq("digitSum(9) = 9", 9, () -> Mine.digitSum(9));

        T.eq("isPrime(1) 는 소수가 아니다", false, () -> Mine.isPrime(1));
        T.eq("isPrime(2)", true, () -> Mine.isPrime(2));
        T.eq("isPrime(29)", true, () -> Mine.isPrime(29));
        T.eq("isPrime(91) = 7 x 13", false, () -> Mine.isPrime(91));
        T.eq("isPrime(97)", true, () -> Mine.isPrime(97));

        T.eq("triangle(3)", "*\n**\n***", () -> Mine.triangle(3));
        T.eq("triangle(1)", "*", () -> Mine.triangle(1));

        T.eq("dayType(\"MON\")", "평일", () -> Mine.dayType("MON"));
        T.eq("dayType(\"SUN\")", "주말", () -> Mine.dayType("SUN"));
        T.eq("dayType(\"???\")", "알수없음", () -> Mine.dayType("???"));
        String src = T.source(args[0] + "/Mine.java");
        T.ok("dayType 을 switch 화살표(->) 로 풀었는가", () -> src.contains("switch") && src.contains("->"),
                "switch 표현식과 화살표(->)를 써서 풀어 보세요");

        T.eq("judge(50, 30)", "UP", () -> Mine.judge(50, 30));
        T.eq("judge(50, 70)", "DOWN", () -> Mine.judge(50, 70));
        T.eq("judge(50, 50)", "CORRECT", () -> Mine.judge(50, 50));

        T.eq("collatzSteps(1) = 0", 0, () -> Mine.collatzSteps(1));
        T.eq("collatzSteps(6) = 8", 8, () -> Mine.collatzSteps(6));
        T.eq("collatzSteps(27) = 111", 111, () -> Mine.collatzSteps(27));
        T.done();
    }
}

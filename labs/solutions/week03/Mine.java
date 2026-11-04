public class Mine {
    static int sumEven(int n) {
        int sum = 0;
        for (int i = 2; i <= n; i += 2) sum += i;
        return sum;
    }

    static String fizzBuzz(int i) {
        if (i % 15 == 0) return "FizzBuzz";
        if (i % 3 == 0) return "Fizz";
        if (i % 5 == 0) return "Buzz";
        return String.valueOf(i);
    }

    static boolean isLeap(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    static int digitSum(int n) {
        int sum = 0;
        while (n > 0) {
            sum += n % 10;
            n /= 10;
        }
        return sum;
    }

    static boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int d = 2; d * d <= n; d++) {
            if (n % d == 0) return false;
        }
        return true;
    }

    static String triangle(int h) {
        StringBuilder sb = new StringBuilder();
        for (int row = 1; row <= h; row++) {
            if (row > 1) sb.append('\n');
            sb.append("*".repeat(row));
        }
        return sb.toString();
    }

    static String dayType(String day) {
        return switch (day) {
            case "MON", "TUE", "WED", "THU", "FRI" -> "평일";
            case "SAT", "SUN" -> "주말";
            default -> "알수없음";
        };
    }

    static String judge(int answer, int guess) {
        if (guess < answer) return "UP";
        if (guess > answer) return "DOWN";
        return "CORRECT";
    }

    static int collatzSteps(int n) {
        int steps = 0;
        while (n != 1) {
            n = n % 2 == 0 ? n / 2 : n * 3 + 1;
            steps++;
        }
        return steps;
    }
}

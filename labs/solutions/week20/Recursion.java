public final class Recursion {
    private Recursion() {}

    public static long sumToRecursive(long n) {
        return n == 0 ? 0 : n + sumToRecursive(n - 1);
    }

    public static long sumTo(long n) {
        long sum = 0;
        for (long i = 1; i <= n; i++) sum += i;
        return sum;
    }

    public static long fib(int n) {
        long a = 0, b = 1;
        for (int i = 0; i < n; i++) {
            long next = a + b;
            a = b;
            b = next;
        }
        return a;
    }
}

/**
 * 20주차 실습 2. 호출 스택(stack)은 크기가 정해져 있습니다.
 * sumToRecursive 는 이미 있습니다. 큰 n 에서 StackOverflowError 가 나는 것을 확인하고,
 * 같은 일을 반복문으로 하는 sumTo, fib 를 만드세요.
 */
public final class Recursion {
    private Recursion() {}

    /** 1 + 2 + ... + n. 고치지 않습니다. */
    public static long sumToRecursive(long n) {
        return n == 0 ? 0 : n + sumToRecursive(n - 1);
    }

    /** sumToRecursive 와 같은 결과를, 반복문으로. n=10_000_000 에서도 죽지 않아야 한다. */
    public static long sumTo(long n) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("sumTo 를 구현하세요");
    }

    /** 피보나치 (fib(0)=0, fib(1)=1). 반복문으로. fib(90) 까지 long 으로 정확해야 한다. */
    public static long fib(int n) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("fib 를 구현하세요");
    }
}

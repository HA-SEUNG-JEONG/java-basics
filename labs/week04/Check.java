import java.util.Arrays;

public class Check {
    public static void main(String[] args) {
        T.eq("max({3, 9, 4}) = 9", 9, () -> Mine.max(new int[]{3, 9, 4}));
        T.eq("max({-5, -2, -9}) = -2  (0으로 시작하면 틀린다)", -2, () -> Mine.max(new int[]{-5, -2, -9}));

        T.eq("isSorted({1, 2, 2, 5})", true, () -> Mine.isSorted(new int[]{1, 2, 2, 5}));
        T.eq("isSorted({1, 3, 2})", false, () -> Mine.isSorted(new int[]{1, 3, 2}));
        T.eq("isSorted({}) / isSorted({7})", true, () -> Mine.isSorted(new int[]{}) && Mine.isSorted(new int[]{7}));

        T.eq("reverse({1, 2, 3}) = {3, 2, 1}", new int[]{3, 2, 1}, () -> Mine.reverse(new int[]{1, 2, 3}));
        int[] original = {1, 2, 3};
        T.ok("reverse 는 원본을 바꾸지 않는다", () -> {
            Mine.reverse(original);
            return Arrays.equals(original, new int[]{1, 2, 3});
        }, "원본 {1,2,3} 이 바뀌었다. 새 배열을 만들어 돌려주세요");

        T.eq("indexesOf({1, 2, 1, 3, 1}, 1) = {0, 2, 4}", new int[]{0, 2, 4},
                () -> Mine.indexesOf(new int[]{1, 2, 1, 3, 1}, 1));
        T.eq("indexesOf({1, 2}, 9) = {}", new int[]{}, () -> Mine.indexesOf(new int[]{1, 2}, 9));

        T.eq("secondMax({5, 9, 9, 3}) = 5", 5, () -> Mine.secondMax(new int[]{5, 9, 9, 3}));
        T.eq("secondMax({1, 2}) = 1", 1, () -> Mine.secondMax(new int[]{1, 2}));
        T.eq("secondMax({9, 8, 7}) = 8", 8, () -> Mine.secondMax(new int[]{9, 8, 7}));

        T.eq("fib(0) = 0", 0, () -> Mine.fib(0));
        T.eq("fib(1) = 1", 1, () -> Mine.fib(1));
        T.eq("fib(10) = 55", 55, () -> Mine.fib(10));
        T.eq("fibLoop(10) = 55", 55L, () -> Mine.fibLoop(10));
        T.eq("fibLoop(50) = 12586269025", 12586269025L, () -> Mine.fibLoop(50));

        int[] arr = {1, 2, 3};
        T.ok("doubleAll 은 전달한 배열 자체를 바꾼다", () -> {
            Mine.doubleAll(arr);
            return Arrays.equals(arr, new int[]{2, 4, 6});
        }, "배열이 {2,4,6} 이 되어야 한다. 새 배열을 만들면 안 된다");

        int[] box = {10};
        T.ok("tryDouble(x) 는 호출한 쪽의 변수를 바꾸지 못한다 (값 복사)", () -> {
            int mine = 10;
            int result = Mine.tryDouble(mine);
            return mine == 10 && result == 20;
        }, "변수는 10 그대로, 반환값은 20 이어야 한다");

        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{10, 20}, {30, 40}};
        T.eq("addMatrix", new int[][]{{11, 22}, {33, 44}}, () -> Mine.addMatrix(a, b));
        T.eq("sumGrid({{1,2},{3,4}}) = 10", 10, () -> Mine.sumGrid(a));
        T.done();
    }
}

public class Mine {
    static int max(int[] a) {
        int m = a[0];
        for (int v : a) if (v > m) m = v;
        return m;
    }

    static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) return false;
        }
        return true;
    }

    static int[] reverse(int[] a) {
        int[] r = new int[a.length];
        for (int i = 0; i < a.length; i++) r[a.length - 1 - i] = a[i];
        return r;
    }

    static int[] indexesOf(int[] a, int target) {
        int count = 0;
        for (int v : a) if (v == target) count++;
        int[] r = new int[count];
        int k = 0;
        for (int i = 0; i < a.length; i++) if (a[i] == target) r[k++] = i;
        return r;
    }

    static int secondMax(int[] a) {
        int first = Integer.MIN_VALUE, second = Integer.MIN_VALUE;
        for (int v : a) {
            if (v > first) {
                second = first;
                first = v;
            } else if (v > second && v != first) {
                second = v;
            }
        }
        return second;
    }

    static int fib(int n) {
        if (n < 2) return n;
        return fib(n - 1) + fib(n - 2);
    }

    static long fibLoop(int n) {
        long a = 0, b = 1;
        for (int i = 0; i < n; i++) {
            long next = a + b;
            a = b;
            b = next;
        }
        return a;
    }

    static void doubleAll(int[] a) {
        for (int i = 0; i < a.length; i++) a[i] *= 2;
    }

    static int tryDouble(int x) {
        x = x * 2;
        return x;
    }

    static int[][] addMatrix(int[][] a, int[][] b) {
        int[][] r = new int[a.length][a[0].length];
        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a[i].length; j++)
                r[i][j] = a[i][j] + b[i][j];
        return r;
    }

    static int sumGrid(int[][] g) {
        int sum = 0;
        for (int[] row : g) for (int v : row) sum += v;
        return sum;
    }
}

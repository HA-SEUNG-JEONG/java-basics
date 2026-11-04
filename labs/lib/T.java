import java.util.Arrays;
import java.util.Objects;

/** 채점 도우미입니다. 읽을 필요 없습니다. */
public class T {
    static int pass, fail;

    /** 예외를 던질 수 있는 Supplier */
    public interface Get<V> { V get() throws Exception; }

    /** 예외를 던질 수 있는 Runnable */
    public interface Block { void run() throws Exception; }

    static String show(Object o) {
        if (o == null) return "null";
        if (o.getClass().isArray()) return Arrays.deepToString(new Object[]{o}).replaceAll("^\\[|\\]$", "");
        if (o instanceof String s) return "\"" + s + "\"";
        return String.valueOf(o);
    }

    static void report(String name, boolean ok, String msg) {
        System.out.println((ok ? "  ✅ " : "  ❌ ") + name + (ok || msg.isEmpty() ? "" : "\n       -> " + msg));
        if (ok) pass++; else fail++;
    }

    /** 값이 같은지 검사. 숫자 타입까지 같아야 한다(int 와 long 은 다르다). */
    public static void eq(String name, Object expected, Get<Object> actual) {
        Object a;
        try {
            a = actual.get();
        } catch (Throwable t) {
            report(name, false, "예외 발생: " + t);
            return;
        }
        boolean ok = Objects.deepEquals(expected, a);
        report(name, ok, "기대 " + show(expected) + " / 실제 " + show(a));
    }

    /** 실수 비교 (오차 1e-6 허용) */
    public static void near(String name, double expected, Get<Double> actual) {
        try {
            double a = actual.get();
            report(name, Math.abs(expected - a) < 1e-6, "기대 " + expected + " / 실제 " + a);
        } catch (Throwable t) {
            report(name, false, "예외 발생: " + t);
        }
    }

    /** 조건이 참인지 검사 */
    public static void ok(String name, Get<Boolean> cond, String hint) {
        try {
            report(name, cond.get(), hint);
        } catch (Throwable t) {
            report(name, false, "예외 발생: " + t);
        }
    }

    /** 특정 예외가 발생하는지 검사 */
    public static void thrown(String name, Class<? extends Throwable> type, Block r) {
        try {
            r.run();
            report(name, false, type.getSimpleName() + " 이(가) 발생해야 하는데 아무 일도 없었다");
        } catch (Throwable t) {
            report(name, type.isInstance(t), type.getSimpleName() + " 기대 / 실제 " + t);
        }
    }

    public static String source(String file) {
        try {
            return java.nio.file.Files.readString(java.nio.file.Path.of(file));
        } catch (Exception e) {
            return "";
        }
    }

    public static int count(String text, String token) {
        return text.split(java.util.regex.Pattern.quote(token), -1).length - 1;
    }

    public static void done() {
        if (fail == 0) {
            System.out.println("\n🎉 전부 통과 (" + pass + "/" + pass + ")");
        } else {
            System.out.println("\n" + fail + "개 실패 / " + (pass + fail) + "개 중. 위 ❌를 위에서부터 하나씩 고쳐 보세요.");
        }
        System.exit(fail == 0 ? 0 : 1);
    }
}

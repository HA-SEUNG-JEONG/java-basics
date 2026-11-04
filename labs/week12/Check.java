import java.util.List;
import java.util.Optional;

public class Check {
    public static void main(String[] args) {
        MyStack<String> s = new MyStack<>();
        T.eq("새 스택은 비어 있다", true, () -> s.isEmpty() && s.size() == 0);
        s.push("a");
        s.push("b");
        T.eq("push 두 번 후 size=2, peek=b", "2:b", () -> s.size() + ":" + s.peek());
        T.eq("pop 은 LIFO", "b:a", () -> s.pop() + ":" + s.pop());
        T.thrown("빈 스택 pop 은 IllegalStateException", IllegalStateException.class, () -> s.pop());
        T.thrown("빈 스택 peek 도 IllegalStateException", IllegalStateException.class, () -> s.peek());
        MyStack<Integer> n = new MyStack<>();
        n.push(5);
        T.eq("MyStack<Integer> 도 동작한다 (캐스팅 없이)", 5, () -> n.pop() + 0);

        Cache<String, Integer> c = new Cache<>();
        c.put("a", 1);
        T.eq("get(있는 키) = Optional.of(1)", Optional.of(1), () -> c.get("a"));
        T.eq("get(없는 키) = Optional.empty()", Optional.empty(), () -> c.get("zzz"));
        c.put("a", 2);
        T.eq("같은 키는 덮어쓴다: size=1, get=2", "1:2", () -> c.size() + ":" + c.get("a").orElse(-1));

        T.eq("maxOf(Integer)", 9, () -> Util.maxOf(List.of(3, 9, 4)));
        T.eq("maxOf(String)", "pear", () -> Util.maxOf(List.of("apple", "pear", "fig")));

        String[] arr = {"x", "y", "z"};
        T.ok("swap(String[])", () -> {
            Util.swap(arr, 0, 2);
            return String.join("", arr).equals("zyx");
        }, "swap 후 {z, y, x} 가 되어야 한다");
        Integer[] nums = {1, 2, 3};
        T.ok("swap(Integer[]) 에도 같은 메서드가 동작한다", () -> {
            Util.swap(nums, 0, 1);
            return nums[0] == 2 && nums[1] == 1;
        }, "{2, 1, 3} 이 되어야 한다");

        T.eq("sumAll(List<Integer>) = 6.0", 6.0, () -> Util.sumAll(List.of(1, 2, 3)));
        T.eq("sumAll(List<Double>) = 4.0", 4.0, () -> Util.sumAll(List.of(1.5, 2.5)));

        T.eq("parse(\"12\") = Optional.of(12)", Optional.of(12), () -> Util.parse("12"));
        T.eq("parse(\"x\") = Optional.empty()", Optional.empty(), () -> Util.parse("x"));
        T.done();
    }
}

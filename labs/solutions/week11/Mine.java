import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class Mine {
    static List<Integer> dedupSorted(List<Integer> nums) {
        return new ArrayList<>(new TreeSet<>(nums));
    }

    static List<String> sortByLenThenAlpha(List<String> words) {
        List<String> copy = new ArrayList<>(words);
        copy.sort(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        return copy;
    }

    static Map<String, Integer> wordFreq(String text) {
        Map<String, Integer> m = new HashMap<>();
        for (String w : text.split(" ")) m.merge(w, 1, Integer::sum);
        return m;
    }

    static boolean isBalanced(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if ("({[".indexOf(c) >= 0) {
                stack.push(c);
            } else if (")}]".indexOf(c) >= 0) {
                if (stack.isEmpty()) return false;
                char open = stack.pop();
                if ("({[".indexOf(open) != ")}]".indexOf(c)) return false;
            }
        }
        return stack.isEmpty();
    }

    static List<Integer> intersection(List<Integer> a, List<Integer> b) {
        TreeSet<Integer> result = new TreeSet<>(a);
        result.retainAll(b);
        return new ArrayList<>(result);
    }

    static List<String> top3Words(String text) {
        Map<String, Integer> freq = wordFreq(text);
        List<String> words = new ArrayList<>(freq.keySet());
        words.sort(Comparator.<String>comparingInt(freq::get).reversed().thenComparing(Comparator.naturalOrder()));
        return words.subList(0, Math.min(3, words.size()));
    }

    static void removeEvens(List<Integer> nums) {
        nums.removeIf(n -> n % 2 == 0);
    }
}

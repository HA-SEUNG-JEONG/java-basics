import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Check {
    public static void main(String[] args) {
        T.eq("dedupSorted({3,1,2,3,1}) = [1,2,3]", List.of(1, 2, 3), () -> Mine.dedupSorted(List.of(3, 1, 2, 3, 1)));
        T.eq("dedupSorted({}) = []", List.of(), () -> Mine.dedupSorted(List.of()));

        List<String> words = new ArrayList<>(List.of("pear", "fig", "apple", "kiwi", "date"));
        T.eq("sortByLenThenAlpha", List.of("fig", "date", "kiwi", "pear", "apple"), () -> Mine.sortByLenThenAlpha(words));
        T.eq("sortByLenThenAlpha 는 원본을 바꾸지 않는다", List.of("pear", "fig", "apple", "kiwi", "date"), () -> words);

        T.eq("wordFreq", Map.of("to", 2, "be", 1), () -> Mine.wordFreq("to be to"));
        T.eq("wordFreq(긴 문장)", Map.of("to", 3, "be", 2, "or", 1, "not", 1),
                () -> Mine.wordFreq("to be or not to be to"));

        T.eq("isBalanced(\"({[]})\")", true, () -> Mine.isBalanced("({[]})"));
        T.eq("isBalanced(\"([)]\")  짝은 맞아도 순서가 틀림", false, () -> Mine.isBalanced("([)]"));
        T.eq("isBalanced(\"((\")", false, () -> Mine.isBalanced("(("));
        T.eq("isBalanced(\")(\")", false, () -> Mine.isBalanced(")("));
        T.eq("isBalanced(\"\")", true, () -> Mine.isBalanced(""));

        T.eq("intersection", List.of(2, 3), () -> Mine.intersection(List.of(3, 2, 2, 1), List.of(2, 3, 4, 3)));
        T.eq("intersection(겹침 없음)", List.of(), () -> Mine.intersection(List.of(1), List.of(2)));

        T.eq("top3Words", List.of("to", "be", "is"),
                () -> Mine.top3Words("to be or not to be that is the question to"));
        T.eq("top3Words(단어 2개뿐)", List.of("a", "b"), () -> Mine.top3Words("b a a"));

        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        T.ok("removeEvens 후 [1,3,5]", () -> {
            Mine.removeEvens(nums);
            return nums.equals(List.of(1, 3, 5));
        }, "순회 중 remove 대신 removeIf 를 써 보세요 (ConcurrentModificationException)");
        T.done();
    }
}

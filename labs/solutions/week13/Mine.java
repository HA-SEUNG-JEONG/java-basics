import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Mine {
    static int sumMultiplesOf3(List<Integer> nums) {
        return nums.stream().filter(n -> n % 3 == 0).mapToInt(Integer::intValue).sum();
    }

    static List<String> longWordsUpper(List<String> words) {
        return words.stream().filter(w -> w.length() >= 3).map(String::toUpperCase).sorted().toList();
    }

    static List<Integer> evensSquared(List<Integer> nums) {
        return nums.stream().filter(n -> n % 2 == 0).map(n -> n * n).toList();
    }

    static Map<String, Integer> totalByCustomer(List<Order> orders) {
        return orders.stream().collect(Collectors.groupingBy(Order::customer, Collectors.summingInt(Order::price)));
    }

    static Optional<String> topCustomer(List<Order> orders) {
        return totalByCustomer(orders).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    static List<String> aboveAverage(Map<String, Integer> scores) {
        double avg = scores.values().stream().mapToInt(Integer::intValue).average().orElse(0);
        return scores.entrySet().stream()
                .filter(e -> e.getValue() >= avg)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    static Map<String, Long> wordFreq(String text) {
        return Arrays.stream(text.split(" ")).collect(Collectors.groupingBy(w -> w, Collectors.counting()));
    }
}

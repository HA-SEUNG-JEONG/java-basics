import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Check {
    public static void main(String[] args) {
        T.eq("sumMultiplesOf3({1..10}) = 18", 18, () -> Mine.sumMultiplesOf3(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
        T.eq("sumMultiplesOf3({}) = 0", 0, () -> Mine.sumMultiplesOf3(List.of()));

        T.eq("longWordsUpper", List.of("BANANA", "FIG", "KIWI", "PEAR"),
                () -> Mine.longWordsUpper(List.of("pear", "fig", "banana", "kiwi", "ab")));
        T.eq("evensSquared", List.of(4, 16, 36), () -> Mine.evensSquared(List.of(1, 2, 3, 4, 5, 6)));

        List<Order> orders = List.of(
                new Order("하승", "도서", 15_000), new Order("민지", "전자", 80_000),
                new Order("하승", "전자", 120_000), new Order("철수", "도서", 22_000),
                new Order("민지", "도서", 9_000));
        T.eq("totalByCustomer", Map.of("하승", 135_000, "민지", 89_000, "철수", 22_000), () -> Mine.totalByCustomer(orders));
        T.eq("topCustomer = 하승", Optional.of("하승"), () -> Mine.topCustomer(orders));
        T.eq("topCustomer(주문 없음) = empty", Optional.empty(), () -> Mine.topCustomer(List.of()));

        T.eq("aboveAverage", List.of("수학", "영어"),
                () -> Mine.aboveAverage(Map.of("국어", 60, "수학", 90, "영어", 80, "과학", 50)));
        T.eq("wordFreq", Map.of("a", 2L, "b", 1L), () -> Mine.wordFreq("a b a"));

        String src = T.source(args[0] + "/Mine.java");
        int used = T.count(src, ".stream()") + T.count(src, "Arrays.stream(");
        T.ok("Stream 을 7개 메서드에서 사용했는가 (현재 " + used + "회)", () -> used >= 7,
                "for/while 대신 .stream() 으로 풀어 보세요 (메서드마다 1회 이상)");
        T.done();
    }
}

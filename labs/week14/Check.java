import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Check {
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("lab14");
        try {
            Path f = dir.resolve("a.txt");
            Files.writeString(f, "안녕 세상\nhello java\n");
            T.eq("wc = {2줄, 4단어, 17글자}", new int[]{2, 4, 17}, () -> Mine.wc(f));

            Path g = dir.resolve("saved.txt");
            T.ok("saveLines 후 파일이 생기고 UTF-8 로 읽힌다", () -> {
                Mine.saveLines(g, List.of("가나다", "abc"));
                return Files.readAllLines(g).equals(List.of("가나다", "abc"));
            }, "Files.write(path, lines) 를 써 보세요");
            T.ok("saveLines 는 덮어쓴다", () -> {
                Mine.saveLines(g, List.of("새내용"));
                return Files.readAllLines(g).equals(List.of("새내용"));
            }, "기존 내용이 남아 있다");

            T.eq("loadLinesOrEmpty(없는 파일) = []", List.of(), () -> Mine.loadLinesOrEmpty(dir.resolve("none.txt")));
            T.eq("loadLinesOrEmpty(있는 파일)", List.of("새내용"), () -> Mine.loadLinesOrEmpty(g));

            Path h = dir.resolve("log.txt");
            T.ok("append 는 없는 파일을 만들고, 이어서 쓴다", () -> {
                Mine.append(h, "one");
                Mine.append(h, "둘");
                return Files.readAllLines(h).equals(List.of("one", "둘"));
            }, "StandardOpenOption.CREATE, APPEND 를 사용하세요");

            Path nums = dir.resolve("nums.txt");
            Files.writeString(nums, "10\nabc\n\n 20 \n3.5\n-7\n");
            T.eq("loadValidInts 는 잘못된 줄을 건너뛴다", List.of(10, 20, -7), () -> Mine.loadValidInts(nums));

            Path tree = dir.resolve("tree");
            Files.createDirectories(tree.resolve("sub"));
            Files.writeString(tree.resolve("a.txt"), "x");
            Files.writeString(tree.resolve("b.txt"), "x");
            Files.writeString(tree.resolve("c.java"), "x");
            Files.writeString(tree.resolve("sub/d.txt"), "x");
            Files.writeString(tree.resolve("Makefile"), "x");
            T.eq("extCount", Map.of("txt", 3, "java", 1, "(none)", 1), () -> Mine.extCount(dir.resolve("tree")));
        } finally {
            try (var s = Files.walk(dir)) {
                s.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
        T.done();
    }
}

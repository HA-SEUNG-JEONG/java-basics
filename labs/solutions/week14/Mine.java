import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Mine {
    static int[] wc(Path p) throws IOException {
        String content = Files.readString(p);
        int lines = Files.readAllLines(p).size();
        int words = 0;
        for (String line : Files.readAllLines(p)) {
            if (!line.isBlank()) words += line.strip().split("\\s+").length;
        }
        return new int[]{lines, words, content.length()};
    }

    static void saveLines(Path p, List<String> lines) throws IOException {
        Files.write(p, lines);
    }

    static List<String> loadLinesOrEmpty(Path p) throws IOException {
        if (Files.notExists(p)) return new ArrayList<>();
        return Files.readAllLines(p);
    }

    static void append(Path p, String line) throws IOException {
        Files.writeString(p, line + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    static List<Integer> loadValidInts(Path p) throws IOException {
        List<Integer> result = new ArrayList<>();
        for (String line : Files.readAllLines(p)) {
            try {
                result.add(Integer.parseInt(line.strip()));
            } catch (NumberFormatException e) {
                // 잘못된 줄은 건너뛴다
            }
        }
        return result;
    }

    static Map<String, Integer> extCount(Path dir) throws IOException {
        Map<String, Integer> counts = new HashMap<>();
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                String name = path.getFileName().toString();
                int dot = name.lastIndexOf('.');
                String ext = dot < 0 ? "(none)" : name.substring(dot + 1);
                counts.merge(ext, 1, Integer::sum);
            });
        }
        return counts;
    }
}

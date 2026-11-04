import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

// 14주차 실습: java.nio.file.Files / Path 를 사용하세요. 채점:  cd labs && ./check.sh 14
public class Mine {
    /** 파일의 {줄 수, 단어 수, 글자 수}. 글자 수는 줄바꿈 문자를 포함한 문자열 길이 */
    static int[] wc(Path p) throws IOException {
        return new int[]{-1, -1, -1}; // 여기를 고치세요
    }

    /** 줄 목록을 UTF-8 파일로 저장 (이미 있으면 덮어쓴다) */
    static void saveLines(Path p, List<String> lines) throws IOException {
        // 여기를 고치세요
    }

    /** 파일의 모든 줄. 파일이 없으면 빈 리스트 (예외를 던지지 않는다) */
    static List<String> loadLinesOrEmpty(Path p) throws IOException {
        return null; // 여기를 고치세요
    }

    /** 한 줄을 파일 끝에 추가한다. 파일이 없으면 만든다 */
    static void append(Path p, String line) throws IOException {
        // 여기를 고치세요
    }

    /** 한 줄에 하나씩 적힌 정수 파일을 읽는다. 빈 줄이나 숫자가 아닌 줄은 건너뛴다 */
    static List<Integer> loadValidInts(Path p) throws IOException {
        return null; // 여기를 고치세요
    }

    /** 디렉터리를 하위 폴더까지 탐색해 확장자별 파일 개수. 확장자가 없으면 키는 "(none)" */
    static Map<String, Integer> extCount(Path dir) throws IOException {
        return null; // 여기를 고치세요
    }
}

# 14주차. 파일 I/O

**예상 시간:** 8시간 | **목표:** 파일을 안전하게 읽고 쓰고, 프로그램의 데이터를 파일에 보존할 수 있다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict14.java
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Predict14 {
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("p14");
        Path f = dir.resolve("a.txt");
        System.out.println(Files.exists(f));
        Files.writeString(f, "가나\nabc");
        System.out.println(Files.size(f));
        System.out.println(Files.readAllLines(f).size());
        Files.writeString(f, "X");
        System.out.println(Files.readString(f));
        Files.writeString(f, "Y", StandardOpenOption.APPEND);
        System.out.println(Files.readString(f));
        System.out.println(f.getFileName() + " " + f.getParent().equals(dir));
        try {
            Files.readString(dir.resolve("none.txt"));
        } catch (NoSuchFileException e) {
            System.out.println("no file");
        }
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
false
10
2
X
XY
a.txt true
no file
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `main` 에서 `throws Exception` 지우기 | `unreported exception java.io.IOException; must be caught or declared to be thrown` |
| `Files.readString(Path.of("없는파일.txt"))` | `NoSuchFileException: 없는파일.txt` — 경로가 에러 메시지에 나온다 |
| `Files.writeString(f, "가나", StandardCharsets.ISO_8859_1)` 후 기본(UTF-8) 으로 읽기 | 한글이 `??` 로 깨진다. 인코딩 불일치 |
| `Files.writeString(f, "A")` 를 두 번 (APPEND 없이) | 내용은 `A` 하나. 기본 동작은 **덮어쓰기** |
| 임시 폴더의 파일을 지우지 않고 `Files.delete(dir)` | `DirectoryNotEmptyException` — 폴더는 비어야 지워진다 |
| `Files.createDirectory(Path.of("a/b/c"))` (부모 `a/b` 없음) | `NoSuchFileException` — 중첩 폴더는 `createDirectories` |

### 3단계. 빈칸에서 만들기 — 자동 채점 (1~3시간)

`../../labs/week14/` 폴더의 `Mine.java`를 열어 `// 여기를 고치세요` 부분을 직접 구현합니다. 구현하고 채점기를 돌려 ✅를 하나씩 늘려 가세요.

```bash
cd labs
./check.sh 14
```

- 처음에는 거의 다 ❌입니다. 정상입니다. **❌ 메시지의 "기대 / 실제"를 읽고** 위에서부터 하나씩 고치세요.
- 컴파일 에러가 나면 첫 번째 에러부터 읽으세요.
- 15분 넘게 막혔을 때만 `../../labs/solutions/week14/`를 열어 보세요. 보고 나서는 **닫고 다시 직접 쳐서** 구현하세요.


---

## 1. 기본 도구: `Path`와 `Files`

옛날 `java.io.File` 대신 `java.nio.file`의 `Path`/`Files`를 쓰세요. 대부분의 작업이 한 줄입니다.

```java
// File: FilesDemo.java
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FilesDemo {
    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("java-basics");
        Path file = dir.resolve("memo.txt");          // 경로 결합: dir/memo.txt

        // 쓰기
        Files.writeString(file, "첫 줄\n둘째 줄\n", StandardCharsets.UTF_8);
        Files.write(file, List.of("셋째 줄"), java.nio.file.StandardOpenOption.APPEND); // 이어쓰기

        // 읽기
        String all = Files.readString(file);
        System.out.print(all);

        List<String> lines = Files.readAllLines(file);
        System.out.println(lines.size() + "줄");       // 3줄

        // 큰 파일은 한 줄씩: Stream은 반드시 닫아야 하므로 try-with-resources
        try (var stream = Files.lines(file)) {
            System.out.println(stream.filter(l -> l.contains("째")).count()); // 2
        }

        // 존재 여부, 메타 정보
        System.out.println(Files.exists(file));
        System.out.println(Files.size(file) + " bytes");
        System.out.println(file.getFileName());       // memo.txt

        // 정리
        Files.delete(file);
        Files.delete(dir);
    }
}
```

- 문자 인코딩은 **항상 UTF-8을 명시**하는 습관을 들이세요. `Files.readString`/`writeString`은 기본이 UTF-8입니다.
- `IOException`은 **checked 예외**입니다. 위처럼 `throws IOException`으로 넘기거나 `try/catch`로 처리해야 컴파일됩니다 (8주차).
- 상대 경로는 프로그램을 **실행한 디렉터리** 기준입니다. 헷갈릴 땐 `Path.of("").toAbsolutePath()`로 확인하세요.

## 2. try-with-resources + Reader/Writer

한 줄씩 읽고 쓰는 전통적인 방식은 여전히 자주 보입니다. 자원은 반드시 try-with-resources로 닫습니다.

```java
// File: ReaderWriterDemo.java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReaderWriterDemo {
    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("log", ".txt");

        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            for (int i = 1; i <= 3; i++) {
                w.write("로그 " + i);
                w.newLine();
            }
        }

        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) {     // 파일 끝이면 null
                System.out.println(line);
            }
        }

        Files.deleteIfExists(file);
    }
}
```

## 3. CSV 읽고 쓰기 (직접 구현)

간단한 데이터는 한 줄에 한 레코드를 `,`로 나눠 저장하는 CSV가 편합니다. 값 안에 쉼표나 따옴표가 있다면 라이브러리(Jackson CSV, OpenCSV)를 쓰세요.

```java
// File: CsvDemo.java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

record Expense(String date, String category, int amount) {
    String toCsv() {
        return date + "," + category + "," + amount;
    }

    static Expense fromCsv(String line) {
        String[] p = line.split(",");
        if (p.length != 3) {
            throw new IllegalArgumentException("형식 오류: " + line);
        }
        return new Expense(p[0], p[1], Integer.parseInt(p[2].trim()));
    }
}

class ExpenseStore {
    private final Path path;

    ExpenseStore(Path path) { this.path = path; }

    void save(List<Expense> expenses) throws IOException {
        Files.write(path, expenses.stream().map(Expense::toCsv).toList());
    }

    List<Expense> load() throws IOException {
        if (Files.notExists(path)) {
            return new ArrayList<>();           // 파일이 없으면 빈 목록
        }
        return Files.readAllLines(path).stream()
                .filter(l -> !l.isBlank())
                .map(Expense::fromCsv)
                .toList();
    }
}

public class CsvDemo {
    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("expenses", ".csv");
        ExpenseStore store = new ExpenseStore(file);

        store.save(List.of(
                new Expense("2026-10-01", "식비", 12_000),
                new Expense("2026-10-02", "교통", 1_500)));

        List<Expense> loaded = store.load();
        System.out.println(loaded.size() + "건");
        System.out.println(loaded.get(0));
        System.out.println(loaded.stream().mapToInt(Expense::amount).sum() + "원");

        Files.deleteIfExists(file);
    }
}
```

`ExpenseStore`는 파일에 대한 책임만 가집니다. 16주차 최종 프로젝트에서 이 구조를 그대로 가져다 씁니다.

## 4. 디렉터리 탐색

```java
// File: WalkDemo.java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WalkDemo {
    public static void main(String[] args) throws IOException {
        Path root = Path.of(".");
        try (var paths = Files.walk(root, 2)) {      // 깊이 2까지
            long javaFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .count();
            System.out.println(".java 파일 수: " + javaFiles);
        }
    }
}
```

## 5. JSON은 어떻게 하나

자바 표준 라이브러리에는 JSON 파서가 없습니다. 실무에서는 **Jackson**을 씁니다 (Spring Boot에 기본 포함).

```java
// 의존성 필요: com.fasterxml.jackson.core:jackson-databind
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(new Point(1, 2));   // {"x":1,"y":2}
Point p = mapper.readValue(json, Point.class);
```

16주차에서 Gradle을 배운 뒤 의존성을 추가해 직접 시도해 볼 수 있습니다.

## 6. 주의 사항

| 상황 | 대응 |
|------|------|
| 한글이 깨진다 | UTF-8을 명시했는지 확인 |
| 파일이 없다 | `Files.exists`로 확인하거나 `NoSuchFileException`을 처리 |
| 쓰는 도중 프로그램이 죽는다 | 임시 파일에 쓴 뒤 `Files.move`로 교체하면 안전 |
| 경로를 문자열로 이어붙인다 | `Path.resolve`를 쓴다. OS마다 구분자가 다르다 |
| 사용자 입력이 경로가 된다 | `../` 같은 경로 탈출을 막아야 한다 (보안) |

## 연습문제

1. 텍스트 파일을 읽어 **줄 수, 단어 수, 글자 수**를 출력하는 `wc` 같은 프로그램을 만드세요.
2. 11주차의 단어 빈도 코드를 파일 입력으로 바꾸세요. 결과도 `result.txt`에 빈도순으로 저장합니다.
3. 메모 앱: `add 내용`, `list`, `delete 번호` 명령을 받는 콘솔 프로그램을 만들되, 데이터는 `memo.txt`에 저장되어 **프로그램을 다시 실행해도 유지**되게 하세요.
4. 위 `CsvDemo`에서 잘못된 줄이 있을 때 프로그램이 멈추지 않고 **그 줄만 건너뛰고** 경고를 출력하게 고치세요.
5. 폴더를 재귀적으로 탐색해 확장자별 파일 개수를 `Map`으로 집계하세요.
6. (도전) 9주차 도서 대출 시스템의 도서·회원 데이터를 CSV로 저장하고 시작할 때 불러오게 하세요.

## 퀴즈

1. 파일을 읽는 코드에서 `IOException`을 처리하지 않으면 어떻게 되나?
2. `Files.lines()`의 결과를 try-with-resources로 감싸야 하는 이유는?
3. 파일 경로를 `"data" + "/" + "a.txt"`로 만드는 방식의 문제는?
4. 한글이 깨지는 대표 원인은?

<details>
<summary>정답</summary>

1. 컴파일 에러 (checked 예외이므로 `throws` 선언이나 `try/catch` 필요)
2. 내부적으로 파일 핸들을 열어 두기 때문에 닫지 않으면 자원이 누수된다
3. OS별 경로 구분자 차이, 중복 슬래시 등. `Path.of("data", "a.txt")`나 `resolve`를 쓴다
4. 읽기와 쓰기의 문자 인코딩 불일치 (UTF-8 명시)

</details>

## 체크리스트

- [ ] `Files.readString`/`writeString`/`readAllLines`를 썼다
- [ ] 파일이 없을 때도 프로그램이 정상 동작한다
- [ ] 데이터를 파일에 저장하고 재실행 후 복원했다
- [ ] `IOException`을 `throws`와 `try/catch`로 각각 처리해 봤다

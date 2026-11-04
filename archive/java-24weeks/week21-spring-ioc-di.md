# 21주차. Spring Boot 입문: IoC와 DI

**예상 시간:** 10시간 | **목표:** Spring이 "객체를 대신 만들고 연결해 주는 컨테이너"라는 것을 직접 만들어 보고, 같은 일을 어노테이션으로 시킨다. 첫 웹 엔드포인트를 띄우고 테스트한다.

> 명령은 모두 `../../labs/backend/` 에서 실행합니다. 처음 실행 시 Spring Boot 라이브러리(수십 MB)를 내려받습니다. 19주차의 의존성 주입(DI)을 먼저 하세요. 이번 주는 그 "조립"을 Spring 이 대신하는 주차입니다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. Spring 없이 "미니 Spring" 만들기 (40분)

Spring 이 하는 일을 50줄로 흉내 냅니다. 이 코드는 **Spring 이 아닙니다.** 클래스 위에 표시(`@Component`)가 있으면 생성자의 매개변수 타입을 보고 필요한 객체를 먼저 만들어서 넣어 주는 일을 하는 작은 컨테이너입니다.

1. 코드를 **실행하기 전에** 출력 6줄을 노트에 먼저 적습니다. 특히 "생성"이 찍히는 **순서**를 맞혀 보세요.
2. 한 글자씩 타이핑하고 `java Predict21.java` 로 실행합니다.
3. 틀린 줄에 ★를 치고 이유를 한 문장으로 적습니다.

```java
// File: Predict21.java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Predict21 {
    @Retention(RetentionPolicy.RUNTIME)
    @interface Component {}

    @Component
    static class Engine {
        Engine() { System.out.println("Engine 생성"); }
    }

    @Component
    static class Car {
        final Engine engine;
        Car(Engine engine) {
            this.engine = engine;
            System.out.println("Car 생성");
        }
    }

    @Component
    static class Driver {
        final Car car;
        final Engine engine;
        Driver(Car car, Engine engine) {
            this.car = car;
            this.engine = engine;
            System.out.println("Driver 생성");
        }
    }

    static final Map<Class<?>, Object> beans = new HashMap<>();

    static <T> T get(Class<T> type) {
        try {
            if (beans.containsKey(type)) return type.cast(beans.get(type));
            if (!type.isAnnotationPresent(Component.class)) {
                throw new IllegalStateException("빈이 아닙니다: " + type.getSimpleName());
            }
            Constructor<?> ctor = type.getDeclaredConstructors()[0];
            Class<?>[] types = ctor.getParameterTypes();
            Object[] args = new Object[types.length];
            for (int i = 0; i < types.length; i++) args[i] = get(types[i]);
            Object bean = ctor.newInstance(args);
            beans.put(type, bean);
            return type.cast(bean);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Driver d = get(Driver.class);
        System.out.println(d.car.engine == d.engine);
        System.out.println(get(Car.class) == d.car);
        try {
            get(String.class);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
Engine 생성
Car 생성
Driver 생성
true
true
빈이 아닙니다: String
```

- `Driver` 를 달라고 했는데 `Engine` 이 **먼저** 만들어집니다. 의존하는 것부터 만들어 넣어 주기 때문입니다.
- `Engine` 은 한 번만 만들어져 `Car` 와 `Driver` 가 **같은 객체**를 공유합니다 (`true`). 이것이 Spring 의 기본인 **싱글톤 빈**입니다.
- 표시가 없는 클래스(`String`)는 컨테이너가 모릅니다. Spring 이 "빈이 없다"는 오류를 내는 이유와 같습니다.

</details>

### 2단계. 일부러 망가뜨리기 (40분)

3단계의 실습을 끝낸 뒤에 해 보세요. 통과한 코드를 하나씩 고장 내고, **어떤 이름의 예외가 나오는지, 메시지가 무엇을 알려 주는지** 읽습니다. Spring 의 에러 메시지는 길지만 첫 줄 하나에 답이 있습니다. 출력이 길면 `../../labs/backend/week21/build/reports/tests/test/index.html` 을 브라우저로 열면 읽기 편합니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `KoreanGreeter` 의 `@Primary` 삭제 | `NoUniqueBeanDefinitionException: ... expected single matching bean but found 2: englishGreeter,koreanGreeter` — `Greeter` 후보가 둘이라 못 고른다 |
| `EnglishGreeter` 의 `@Component` 삭제 | `NoSuchBeanDefinitionException: No qualifying bean of type 'shop.Greeter' ... @Qualifier("englishGreeter")` — 이름표를 붙인 대상이 빈이 아니다 |
| `@Value("${app.greeting:안녕}")` 의 `:안녕` 을 지우고 설정 파일에서도 `app.greeting` 삭제 | `Could not resolve placeholder 'app.greeting' in value "${app.greeting}"` — 기본값이 없으면 설정이 빠졌을 때 시작조차 못 한다 |
| `src/main/resources/application.yml` 을 `application.properties` 로 바꿔 `app.greeting=안녕` | 응답의 `안녕` 이 `ì` 같은 깨진 글자로 나온다. Spring Boot 는 `.properties` 를 ISO-8859-1 로 읽기 때문이다. 한글 설정은 `.yml` (UTF-8) 에 둔다 |
| `ShopApplication.java` 를 `shop` 패키지 밖(예: `app` 패키지)으로 옮기기 | 테스트가 `Unable to find a @SpringBootConfiguration by searching packages upwards from the test` 로 실패한다. Spring 은 테스트 패키지에서 **위로** 올라가며 설정 클래스를 찾고, 실행 시에는 설정 클래스가 있는 패키지 **아래만** 컴포넌트를 탐색한다 |

### 3단계. 빈칸에서 만들기 — 자동 채점 (3~4시간)

`../../labs/backend/week21/src/main/java/shop/` 의 스텁을 채웁니다. 파일마다 위에 해야 할 일이 적혀 있습니다.

| 파일 | 해야 할 일 |
|---|---|
| `KoreanGreeter` | 빈으로 등록, 기본 후보로 지정(`@Primary`), 설정값 `app.greeting` 주입(`@Value`) |
| `EnglishGreeter` | 빈으로 등록 |
| `VisitCounter` | 빈으로 등록 (싱글톤으로 공유) |
| `GreetingController` | REST 컨트롤러로 만들고 `/greet`, `/greet/en`, `/visits` 연결 |

```bash
cd ../../labs/backend
./gradlew :week21:test
```

- 처음에는 7개가 모두 실패합니다. 정상입니다.
- 테스트가 통과하면 서버를 직접 띄워 보세요.

  ```bash
  ./gradlew :week21:bootRun
  ```

  다른 터미널에서 (URL 에 `?` 가 있으면 따옴표로 감싸야 zsh 에서 오류가 나지 않습니다):

  ```bash
  curl -G --data-urlencode "name=하승" localhost:8080/greet
  curl "localhost:8080/greet/en?name=Ha"
  curl localhost:8080/visits
  ```

  `Ctrl+C` 로 종료합니다. 방문 횟수가 요청마다 올라가는 것을 확인하세요. (싱글톤)
- 정답은 `../../labs/solutions/week21/shop/` 에 있습니다. **15분 넘게 막혔을 때만** 열고, 본 뒤에는 덮고 **직접 다시 치세요.**

---

## 1. IoC 와 DI

- **IoC(제어의 역전):** 객체를 만들고 연결하는 제어권이 내 코드에서 컨테이너로 넘어간다. 1단계의 `get` 이 그 컨테이너의 핵심이다.
- **DI(의존성 주입):** 필요한 객체를 스스로 만들지 않고 받는다. 19주차에서 `main` 이 `new` 로 하던 조립을 컨테이너가 한다.
- **빈(bean):** 컨테이너가 만들고 관리하는 객체.

> **JS와 비교:** NestJS 의 `@Injectable()` 과 생성자 주입이 거의 같은 모델입니다. Express 에는 이런 컨테이너가 기본으로 없어서 직접 조립하는 코드를 쓰게 됩니다.

## 2. 이번 주에 쓰는 어노테이션

| 어노테이션 | 하는 일 | 실습 위치 |
|---|---|---|
| `@SpringBootApplication` | 설정 + 자동 구성 + 이 패키지 아래 컴포넌트 탐색. `main` 이 있는 클래스에 하나 | `ShopApplication` (주어진 코드) |
| `@Component` / `@Service` | 빈으로 등록 (`@Service` 는 의미를 밝히는 이름표) | `KoreanGreeter` 등 |
| `@RestController` | 이 클래스의 메서드 반환값을 **응답 본문**으로 쓰는 웹 컨트롤러 | `GreetingController` |
| `@GetMapping("/경로")` | GET 요청을 이 메서드에 연결 | 18주차 `handle` 의 `switch` 를 대체 |
| `@RequestParam` | 쿼리 문자열 `?name=` 을 매개변수로 | `defaultValue` 로 기본값 |
| `@Primary` | 같은 타입 후보가 여럿일 때 기본으로 고름 | `KoreanGreeter` |
| `@Qualifier("이름")` | 후보가 여럿일 때 이름으로 지정. 이름은 기본적으로 **클래스명의 첫 글자를 소문자로** (`englishGreeter`) | `GreetingController` |
| `@Value("${키:기본값}")` | 설정 파일의 값을 주입 | `app.greeting` |

생성자가 하나뿐이면 `@Autowired` 를 붙이지 않아도 Spring 이 그 생성자로 주입합니다. 생성자 주입이 필드 주입(`@Autowired private ...`)보다 권장되는 이유는, 필드를 `final` 로 둘 수 있고 테스트에서 `new` 로 조립할 수 있기 때문입니다. 19주차의 방식 그대로입니다.

## 3. 설정 파일

`src/main/resources/application.yml` 이 설정 파일입니다.

```yaml
app:
  greeting: 안녕
```

같은 값을 코드 수정 없이 바꿀 수 있습니다. 테스트에서는 `@SpringBootTest(properties = "app.greeting=반갑습니다")` 처럼 한 클래스에서만 덮어쓸 수 있고, 실행 시에는 `./gradlew :week21:bootRun --args='--app.greeting=hello'` 처럼 명령 줄로도 덮어씁니다. 설정을 코드 밖으로 빼는 이 습관이 환경(개발/운영)마다 다른 값을 쓰는 방법입니다.

## 4. 테스트

`@SpringBootTest` 는 실제 컨텍스트를 띄우고, `@AutoConfigureMockMvc` 의 `MockMvc` 는 서버 포트를 열지 않고 요청을 흉내 냅니다.

```java
@SpringBootTest
@AutoConfigureMockMvc
class GreetingTest {
    @Autowired MockMvc mvc;

    @Test
    void greet() throws Exception {
        String body = mvc.perform(get("/greet").param("name", "하승")).andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals("안녕, 하승!", body);
    }
}
```

- 컨텍스트는 한 번 만들어 테스트 클래스들이 재사용합니다. 빈은 싱글톤이라 테스트 사이에 상태가 남습니다. 그래서 주어진 테스트가 `@BeforeEach` 에서 `counter.reset()` 을 부릅니다. **싱글톤 + 상태 = 테스트 간 오염**이라는 것을 기억하세요.
- 응답 본문을 문자열로 꺼낼 때 `UTF_8` 을 지정하지 않으면 한글이 깨집니다.

## 연습문제

1. `GreetingController` 의 `VisitCounter` 를 `new VisitCounter()` 로 직접 만들게 고치면 어떤 테스트가 실패하나요? 이유를 한 문장으로 적으세요.
2. `Greeter` 구현체를 하나 더(`JapaneseGreeter`) 추가하세요. 컨트롤러를 수정하지 않고 `/greet/ja` 를 지원하려면 어떻게 해야 할까요? (힌트: `Map<String, Greeter>` 주입은 빈 이름을 키로 모아 줍니다)
3. `bootRun` 시작 로그에서 `Tomcat started on port 8080` 줄을 찾고, 포트를 `server.port: 9090` 설정으로 바꿔 보세요.
4. 1단계 미니 컨테이너에 같은 타입의 후보가 둘일 때 `@Primary` 같은 규칙을 직접 추가해 보세요.

## 퀴즈

1. IoC 와 DI 의 관계를 한 문장으로 설명하세요.
2. 빈은 기본적으로 몇 개 만들어지고, 그 때문에 어떤 주의가 필요한가?
3. 같은 타입의 빈이 둘일 때 필요한 두 가지 해결법은?
4. `@SpringBootApplication` 클래스를 다른 패키지로 옮기면 안 되는 이유는?

<details>
<summary>정답</summary>

1. IoC 는 "객체 생성·연결의 제어권을 컨테이너가 가진다"는 원리이고, DI 는 그 원리를 "필요한 객체를 받아서 쓴다"는 방식으로 구현한 것
2. 기본은 이름당 하나(싱글톤). 상태를 가진 빈은 여러 요청·테스트에서 공유되므로 변경에 주의
3. `@Primary` 로 기본 후보를 정하거나, `@Qualifier` 로 이름을 지정
4. 컴포넌트 탐색이 그 클래스가 있는 패키지와 하위 패키지만 대상으로 하기 때문

</details>

## 체크리스트

- [ ] 미니 컨테이너의 `get` 이 하는 일을 내 말로 설명할 수 있다
- [ ] `NoUniqueBeanDefinitionException` 과 `NoSuchBeanDefinitionException` 을 직접 보고, 각각 무엇을 고치라는 뜻인지 안다
- [ ] 테스트 7개를 모두 통과했고 `curl` 로 직접 호출해 봤다
- [ ] `application.properties` 에서 한글이 깨지는 이유를 설명할 수 있다

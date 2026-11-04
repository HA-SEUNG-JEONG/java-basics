import java.util.List;

public class Resources {
    /**
     * try-with-resources 로 Res("A"), Res("B") 를 열고 b.use(fail) 을 호출한다.
     * IllegalStateException 이 나면 catch 해서 LOG 에 "caught" 를 추가한다.
     * 마지막에 LOG 의 복사본을 돌려준다. (시작할 때 Res.LOG.clear() 를 먼저 호출)
     * 기대 순서: [open A, open B, close B, close A] (+ 예외 시 caught)
     */
    static List<String> run(boolean fail) {
        return List.of(); // 여기를 고치세요
    }
}

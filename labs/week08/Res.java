import java.util.ArrayList;
import java.util.List;

// 이 파일은 수정하지 마세요. 열고 닫을 때 LOG 에 기록을 남기는 자원이다.
public class Res implements AutoCloseable {
    static final List<String> LOG = new ArrayList<>();
    private final String name;

    Res(String name) {
        this.name = name;
        LOG.add("open " + name);
    }

    void use(boolean fail) {
        if (fail) throw new IllegalStateException("boom");
    }

    @Override
    public void close() {
        LOG.add("close " + name);
    }
}

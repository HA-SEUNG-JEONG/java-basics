import java.util.ArrayList;
import java.util.List;

// 12주차 실습 1: 제네릭 스택. 내부 저장소로 List<T> 를 사용한다.
// 비어 있을 때 pop/peek 은 IllegalStateException.  채점:  cd labs && ./check.sh 12
public class MyStack<T> {
    private final List<T> items = new ArrayList<>();

    void push(T item) {
        // 여기를 고치세요
    }

    T pop() {
        return null; // 여기를 고치세요
    }

    T peek() {
        return null; // 여기를 고치세요
    }

    boolean isEmpty() {
        return false; // 여기를 고치세요
    }

    int size() {
        return -1; // 여기를 고치세요
    }
}

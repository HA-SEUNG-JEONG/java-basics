import java.util.ArrayList;
import java.util.List;

public class MyStack<T> {
    private final List<T> items = new ArrayList<>();

    void push(T item) {
        items.add(item);
    }

    T pop() {
        if (items.isEmpty()) throw new IllegalStateException("스택이 비었습니다");
        return items.remove(items.size() - 1);
    }

    T peek() {
        if (items.isEmpty()) throw new IllegalStateException("스택이 비었습니다");
        return items.get(items.size() - 1);
    }

    boolean isEmpty() {
        return items.isEmpty();
    }

    int size() {
        return items.size();
    }
}

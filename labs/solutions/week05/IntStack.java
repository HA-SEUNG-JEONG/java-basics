public class IntStack {
    private final int[] data;
    private int size = 0;

    IntStack(int capacity) {
        data = new int[capacity];
    }

    void push(int value) {
        if (size == data.length) {
            throw new IllegalStateException("스택이 가득 찼습니다");
        }
        data[size++] = value;
    }

    int pop() {
        if (size == 0) {
            throw new IllegalStateException("스택이 비었습니다");
        }
        return data[--size];
    }

    int peek() {
        if (size == 0) {
            throw new IllegalStateException("스택이 비었습니다");
        }
        return data[size - 1];
    }

    boolean isEmpty() { return size == 0; }

    int size() { return size; }
}

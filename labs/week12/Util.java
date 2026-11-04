import java.util.List;
import java.util.Optional;

public class Util {
    /** 리스트의 최댓값. 비교 가능한 타입만 받는다 (T extends Comparable<T>) */
    static <T extends Comparable<T>> T maxOf(List<T> list) {
        return null; // 여기를 고치세요
    }

    /** 배열의 i, j 위치 값을 맞바꾼다. 어떤 참조 타입 배열에도 동작해야 한다 */
    static <T> void swap(T[] arr, int i, int j) {
        // 여기를 고치세요
    }

    /** Integer, Double 등 모든 Number 리스트의 합 (? extends Number) */
    static double sumAll(List<? extends Number> nums) {
        return -1; // 여기를 고치세요
    }

    /** 정수로 바꿀 수 있으면 Optional.of(값), 아니면 Optional.empty() */
    static Optional<Integer> parse(String s) {
        return null; // 여기를 고치세요
    }
}

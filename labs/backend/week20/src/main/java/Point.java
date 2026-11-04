/**
 * 20주차 실습 3. equals/hashCode 가 없으면 HashSet/HashMap 에서 "같은 점" 이 계속 쌓입니다. (메모리 낭비의 흔한 원인)
 * x, y 가 같으면 같은 점이 되도록 equals 와 hashCode 를 직접 작성하세요. (record 로 바꾸지 말고 직접)
 */
public final class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() { return x; }
    public int y() { return y; }

    // 여기를 고치세요: equals, hashCode
}

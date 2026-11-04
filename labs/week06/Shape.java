// 6주차 실습 (이 파일은 수정하지 마세요). 채점:  cd labs && ./check.sh 06
public abstract class Shape {
    abstract double area();

    String describe() {
        return getClass().getSimpleName() + " 넓이=" + String.format("%.2f", area());
    }
}

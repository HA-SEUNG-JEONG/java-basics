// 부모 클래스 (이 파일은 수정하지 마세요). 기본 월급 = base
public class Employee {
    protected final String name;
    protected final int base;

    Employee(String name, int base) {
        this.name = name;
        this.base = base;
    }

    int monthlyPay() {
        return base;
    }

    String getName() {
        return name;
    }
}

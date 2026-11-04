import java.lang.reflect.Modifier;

public class Check {
    public static void main(String[] args) {
        T.near("Circle(1).area() = PI", Math.PI, () -> new Circle(1).area());
        T.near("Circle(2).area() = 4PI", 4 * Math.PI, () -> new Circle(2).area());
        T.near("Rect(2, 3).area() = 6", 6, () -> new Rect(2, 3).area());
        T.near("Triangle(4, 5).area() = 10", 10, () -> new Triangle(4, 5).area());

        Shape[] shapes = {new Circle(1), new Rect(2, 3), new Triangle(4, 5)};
        T.near("total(shapes) = PI + 6 + 10", Math.PI + 16, () -> Shapes.total(shapes));
        T.eq("largest(shapes) 는 Triangle(넓이 10)", "Triangle", () -> Shapes.largest(shapes).getClass().getSimpleName());
        T.eq("describe() 가 자식의 area() 를 호출한다", "Rect 넓이=6.00", () -> new Rect(2, 3).describe());

        String src = T.source(args[0] + "/Shapes.java");
        T.ok("Shapes 에서 instanceof 를 쓰지 않았는가", () -> !src.replaceAll("//.*", "").contains("instanceof"),
                "if (s instanceof Circle) ... 대신 s.area() 한 줄이면 된다");
        T.ok("Shape 는 추상 클래스다", () -> Modifier.isAbstract(Shape.class.getModifiers()), "수정하지 말아야 할 파일을 건드렸나요?");

        T.eq("Employee 기본: monthlyPay() = base", 3000, () -> new Employee("a", 3000).monthlyPay());
        T.eq("Manager(base 5000, bonus 1000) = 6000", 6000, () -> new Manager("m", 5000, 1000).monthlyPay());
        T.eq("Engineer(base 4000, 야근 10시간) = 4000 + 200000", 204000, () -> new Engineer("e", 4000, 10).monthlyPay());

        Employee[] staff = {new Employee("a", 3000), new Manager("m", 5000, 1000), new Engineer("e", 4000, 10)};
        T.eq("Payroll.total = 3000 + 6000 + 204000", 213000L, () -> Payroll.total(staff));
        String src2 = T.source(args[0] + "/Payroll.java");
        T.ok("Payroll 에서도 instanceof 없이 풀었는가", () -> !src2.replaceAll("//.*|/\\*\\*.*", "").contains("instanceof"),
                "e.monthlyPay() 만 호출하면 각 객체가 알아서 자기 계산을 한다");
        T.done();
    }
}

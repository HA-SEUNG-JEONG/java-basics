public class Engineer extends Employee {
    private final int overtimeHours;

    Engineer(String name, int base, int overtimeHours) {
        super(name, base);
        this.overtimeHours = overtimeHours;
    }

    @Override
    int monthlyPay() {
        return base + overtimeHours * 20000;
    }
}

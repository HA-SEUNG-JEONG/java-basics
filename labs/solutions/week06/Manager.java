public class Manager extends Employee {
    private final int bonus;

    Manager(String name, int base, int bonus) {
        super(name, base);
        this.bonus = bonus;
    }

    @Override
    int monthlyPay() {
        return base + bonus;
    }
}

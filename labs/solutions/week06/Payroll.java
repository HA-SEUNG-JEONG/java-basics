public class Payroll {
    static long total(Employee[] staff) {
        long sum = 0;
        for (Employee e : staff) sum += e.monthlyPay();
        return sum;
    }
}

public final class DiscountPolicies {
    private DiscountPolicies() {}

    public static DiscountPolicy forGrade(String grade) {
        if (grade == null) throw new IllegalArgumentException("알 수 없는 등급: null");
        return switch (grade) {
            case "VIP" -> new PercentDiscount(20);
            case "GOLD" -> new PercentDiscount(10);
            case "NEW" -> new FixedDiscount(3000);
            case "NORMAL" -> new NoDiscount();
            default -> throw new IllegalArgumentException("알 수 없는 등급: " + grade);
        };
    }
}

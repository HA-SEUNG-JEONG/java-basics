public record Money(long amount, String currency) {
    public Money {
        if (amount < 0) {
            throw new IllegalArgumentException("금액은 음수일 수 없습니다: " + amount);
        }
    }

    Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("통화가 다릅니다: " + currency + " vs " + other.currency);
        }
        return new Money(amount + other.amount, currency);
    }
}

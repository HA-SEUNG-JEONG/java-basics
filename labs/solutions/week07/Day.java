public enum Day {
    MON, TUE, WED, THU, FRI, SAT, SUN;

    boolean isWeekend() {
        return this == SAT || this == SUN;
    }

    Day next() {
        return values()[(ordinal() + 1) % values().length];
    }
}

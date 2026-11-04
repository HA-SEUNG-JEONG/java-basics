public enum Day {
    MON, TUE, WED, THU, FRI, SAT, SUN;

    /** 토/일이면 true */
    boolean isWeekend() {
        return false; // 여기를 고치세요
    }

    /** 다음 요일. SUN 의 다음은 MON (힌트: values(), ordinal()) */
    Day next() {
        return this; // 여기를 고치세요
    }
}

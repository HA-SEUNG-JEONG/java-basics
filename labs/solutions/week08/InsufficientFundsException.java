public class InsufficientFundsException extends RuntimeException {
    private final long shortage;

    InsufficientFundsException(long shortage) {
        super("잔액이 " + shortage + "원 부족합니다");
        this.shortage = shortage;
    }

    long getShortage() {
        return shortage;
    }
}

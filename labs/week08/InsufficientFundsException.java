// 8주차 실습 1: 사용자 정의 예외를 완성하세요. 채점:  cd labs && ./check.sh 08
//  - 메시지: "잔액이 500원 부족합니다" (500 은 부족한 금액)
//  - getShortage() 는 부족한 금액을 돌려준다
public class InsufficientFundsException extends RuntimeException {
    InsufficientFundsException(long shortage) {
        super(""); // 여기를 고치세요
    }

    long getShortage() {
        return 0; // 여기를 고치세요
    }
}

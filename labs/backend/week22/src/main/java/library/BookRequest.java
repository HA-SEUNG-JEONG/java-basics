package library;

/**
 * 책 등록/수정 요청 본문.
 * 해야 할 일: title 과 author 가 비어 있거나 공백뿐이면 요청을 거부하도록 검증 표시 붙이기.
 */
public record BookRequest(String title, String author) {}

package library;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * 요청·응답 DTO 모음. 엔티티는 DB 모양, 이것들은 API 계약이다.
 * 응답 필드의 REQUIRED 표시는 OpenAPI → TypeScript 변환 때 "항상 있는 필드" 가 되게 한다.
 */
public final class Dto {
    private Dto() {}

    public record BookRequest(@NotBlank String isbn, @NotBlank String title, @NotBlank String author) {}

    public record MemberRequest(@NotBlank String name) {}

    public record LoanRequest(@NotNull Long memberId, @NotNull Long bookId) {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record BookResponse(
            @Schema(requiredMode = REQUIRED) long id,
            @Schema(requiredMode = REQUIRED) String isbn,
            @Schema(requiredMode = REQUIRED) String title,
            @Schema(requiredMode = REQUIRED) String author) {
        static BookResponse from(Book b) {
            return new BookResponse(b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor());
        }
    }

    public record MemberResponse(
            @Schema(requiredMode = REQUIRED) long id,
            @Schema(requiredMode = REQUIRED) String name) {
        static MemberResponse from(Member m) {
            return new MemberResponse(m.getId(), m.getName());
        }
    }

    public record LoanResponse(
            @Schema(requiredMode = REQUIRED) long id,
            @Schema(requiredMode = REQUIRED) BookResponse book,
            @Schema(requiredMode = REQUIRED) MemberResponse member,
            @Schema(requiredMode = REQUIRED) Instant loanedAt,
            @Schema(requiredMode = REQUIRED, types = {"string", "null"}, format = "date-time",
                    description = "반납 전에는 null") Instant returnedAt) {
        static LoanResponse from(Loan l) {
            return new LoanResponse(l.getId(), BookResponse.from(l.getBook()), MemberResponse.from(l.getMember()),
                    l.getLoanedAt(), l.getReturnedAt());
        }
    }

    public record MeResponse(@Schema(requiredMode = REQUIRED) String username) {}

    /** 모든 오류 응답 본문: {"message": "..."} */
    public record ApiError(@Schema(requiredMode = REQUIRED) String message) {}
}

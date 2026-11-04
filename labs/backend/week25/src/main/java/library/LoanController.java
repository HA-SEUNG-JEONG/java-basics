package library;

import library.Dto.ApiError;
import library.Dto.LoanRequest;
import library.Dto.LoanResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans")
public class LoanController {
    private final LibraryService service;

    public LoanController(LibraryService service) {
        this.service = service;
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "대출됨")
    @ApiResponse(responseCode = "400", description = "검증 실패", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "회원 또는 책 없음", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "이미 대출 중인 책, 또는 회원이 이미 3권 대출 중", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<LoanResponse> lend(@Valid @RequestBody LoanRequest req) {
        LoanResponse loan = service.lend(req.memberId(), req.bookId());
        return ResponseEntity.created(URI.create("/loans/" + loan.id())).body(loan);
    }

    @PostMapping("/{id}/return")
    @ApiResponse(responseCode = "200", description = "반납됨 (returnedAt 채움)")
    @ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "대출 없음", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "이미 반납됨", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public LoanResponse giveBack(@PathVariable long id) {
        return service.giveBack(id);
    }
}

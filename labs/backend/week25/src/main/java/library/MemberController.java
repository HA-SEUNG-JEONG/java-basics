package library;

import library.Dto.ApiError;
import library.Dto.LoanResponse;
import library.Dto.MemberRequest;
import library.Dto.MemberResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final LibraryService service;

    public MemberController(LibraryService service) {
        this.service = service;
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "등록됨. Location 헤더에 /members/{id}")
    @ApiResponse(responseCode = "400", description = "검증 실패", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest req) {
        MemberResponse member = service.createMember(req);
        return ResponseEntity.created(URI.create("/members/" + member.id())).body(member);
    }

    @GetMapping("/{id}/loans")
    @ApiResponse(responseCode = "200", description = "반납하지 않은 대출만")
    @ApiResponse(responseCode = "404", description = "회원 없음", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public List<LoanResponse> activeLoans(@PathVariable long id) {
        return service.activeLoans(id);
    }
}

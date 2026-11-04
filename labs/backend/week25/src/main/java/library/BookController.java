package library;

import library.Dto.ApiError;
import library.Dto.BookRequest;
import library.Dto.BookResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
public class BookController {
    private final LibraryService service;

    public BookController(LibraryService service) {
        this.service = service;
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "등록됨. Location 헤더에 /books/{id}")
    @ApiResponse(responseCode = "400", description = "검증 실패", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "isbn 중복", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest req) {
        BookResponse book = service.createBook(req);
        return ResponseEntity.created(URI.create("/books/" + book.id())).body(book);
    }

    @GetMapping
    @ApiResponse(responseCode = "200", description = "keyword 가 있으면 제목에 포함된 책만 (대소문자 무시)")
    public List<BookResponse> search(@RequestParam(required = false) String keyword) {
        return service.searchBooks(keyword);
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "책")
    @ApiResponse(responseCode = "400", description = "id 가 숫자가 아님", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "책 없음", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public BookResponse get(@PathVariable long id) {
        return service.getBook(id);
    }
}

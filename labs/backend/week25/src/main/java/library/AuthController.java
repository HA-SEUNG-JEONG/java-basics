package library;

import library.Dto.ApiError;
import library.Dto.LoginRequest;
import library.Dto.MeResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

/** 세션 쿠키 로그인. 로그인에 성공하면 응답의 Set-Cookie: JSESSIONID 를 브라우저가 이후 요청에 싣는다. */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contexts = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    @ApiResponse(responseCode = "200", description = "로그인됨. Set-Cookie 로 세션 쿠키(HttpOnly, SameSite=Lax)")
    @ApiResponse(responseCode = "400", description = "검증 실패", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호가 틀림", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public MeResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        // 틀리면 BadCredentialsException -> ApiExceptionHandler 가 401 로 바꾼다
        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(req.username(), req.password()));
        request.getSession();
        request.changeSessionId(); // 세션 고정 공격 방지: 로그인 전 세션 id 를 버린다
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        return new MeResponse(auth.getName());
    }

    @PostMapping("/logout")
    @ApiResponse(responseCode = "204", description = "로그아웃됨 (세션 무효화)")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response,
                                       @Parameter(hidden = true) Authentication auth) {
        new SecurityContextLogoutHandler().logout(request, response, auth);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @ApiResponse(responseCode = "200", description = "현재 로그인한 사용자")
    @ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public MeResponse me(@Parameter(hidden = true) Authentication auth) {
        return new MeResponse(auth.getName());
    }
}

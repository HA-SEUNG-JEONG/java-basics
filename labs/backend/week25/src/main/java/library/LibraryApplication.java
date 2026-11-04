package library;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** 24주차 명세를 구현한 참조 API. 스펙: GET /v3/api-docs, 화면: /swagger-ui.html */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "도서관 API", version = "1.0",
        description = "24주차 도서관 REST API. 오류 응답은 모두 {\"message\": \"...\"} 형식입니다."))
public class LibraryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

    /** 서비스가 시계를 주입받아야 테스트에서 시간을 고정할 수 있다. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}

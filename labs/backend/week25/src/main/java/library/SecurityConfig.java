package library;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) throws Exception {
        return http
                // 데모라서 CSRF 를 끈다(SameSite=Lax 쿠키가 다른 사이트의 POST 에 안 실리는 것에만 기댐). 운영에서는 CookieCsrfTokenRepository 로 켜고 프론트가 X-XSRF-TOKEN 헤더로 보낸다.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults()) // 아래 corsConfigurationSource 빈을 쓴다
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/books", "/members", "/loans", "/loans/*/return").authenticated()
                        .requestMatchers("/auth/me").authenticated()
                        .anyRequest().permitAll())
                // 로그인 안 한 요청의 401 도 ApiExceptionHandler 가 {"message"} 로 만든다
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        (request, response, ex) -> resolver.resolveException(request, response, null, ex)))
                .requestCache(AbstractHttpConfigurer::disable) // 401 응답 때 쓸데없이 세션을 만들지 않게
                .logout(AbstractHttpConfigurer::disable)       // 로그아웃은 AuthController 의 POST /auth/logout
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("http://localhost:3000"));
        cors.setAllowedMethods(List.of("GET", "POST"));
        cors.setAllowedHeaders(List.of("Content-Type"));
        cors.setAllowCredentials(true); // 쿠키를 실은 요청 허용. 이때 origin 에 "*" 는 쓸 수 없다
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /** 고정 데모 사용자 한 명. 비밀번호는 설정값(library.demo.password)에서 읽어 해시로 보관한다. */
    @Bean
    UserDetailsService users(@Value("${library.demo.username}") String username,
                             @Value("${library.demo.password}") String password,
                             PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername(username).password(encoder.encode(password)).roles("USER").build());
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

package shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 이 클래스가 있는 패키지(shop) 아래의 @Component 들을 Spring 이 자동으로 찾습니다. 고치지 않습니다. */
@SpringBootApplication
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}

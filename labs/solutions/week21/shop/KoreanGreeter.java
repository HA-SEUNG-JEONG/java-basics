package shop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class KoreanGreeter implements Greeter {
    private final String greeting;

    public KoreanGreeter(@Value("${app.greeting:안녕}") String greeting) {
        this.greeting = greeting;
    }

    @Override
    public String greet(String name) {
        return greeting + ", " + name + "!";
    }
}

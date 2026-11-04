package shop;

import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {
    private final Greeter greeter;
    private final Greeter english;
    private final VisitCounter counter;

    public GreetingController(Greeter greeter, @Qualifier("englishGreeter") Greeter english, VisitCounter counter) {
        this.greeter = greeter;
        this.english = english;
        this.counter = counter;
    }

    @GetMapping("/greet")
    public String greet(@RequestParam(defaultValue = "손님") String name) {
        counter.increment();
        return greeter.greet(name);
    }

    @GetMapping("/greet/en")
    public String greetEnglish(@RequestParam(defaultValue = "손님") String name) {
        counter.increment();
        return english.greet(name);
    }

    @GetMapping("/visits")
    public Map<String, Integer> visits() {
        return Map.of("count", counter.current());
    }
}

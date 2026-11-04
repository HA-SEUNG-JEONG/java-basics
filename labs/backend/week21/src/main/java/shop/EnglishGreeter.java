package shop;

/** greet("Ha") -> "Hello, Ha!"  이 클래스도 Spring 이 관리하게 만드세요. */
public class EnglishGreeter implements Greeter {
    @Override
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}

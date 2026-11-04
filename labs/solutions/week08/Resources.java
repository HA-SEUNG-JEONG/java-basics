import java.util.List;

public class Resources {
    static List<String> run(boolean fail) {
        Res.LOG.clear();
        try (Res a = new Res("A"); Res b = new Res("B")) {
            b.use(fail);
        } catch (IllegalStateException e) {
            Res.LOG.add("caught");
        }
        return List.copyOf(Res.LOG);
    }
}

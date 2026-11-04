public class Shapes {
    static double total(Shape[] shapes) {
        double sum = 0;
        for (Shape s : shapes) sum += s.area();
        return sum;
    }

    static Shape largest(Shape[] shapes) {
        Shape best = shapes[0];
        for (Shape s : shapes) if (s.area() > best.area()) best = s;
        return best;
    }
}

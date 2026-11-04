// id 와 name 이 모두 같으면 같은 상품이다. equals 와 hashCode 를 함께 재정의하세요. (java.util.Objects 활용)
public class Product {
    private final int id;
    private final String name;

    Product(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // 여기에 equals, hashCode 를 작성하세요

    @Override
    public String toString() {
        return "Product[" + id + ", " + name + "]";
    }
}

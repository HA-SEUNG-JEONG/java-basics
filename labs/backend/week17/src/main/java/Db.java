import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/** 실습용 DB. 테스트마다 새 메모리 DB 를 만들어 줍니다. 이 파일은 고치지 않습니다. */
public final class Db {
    private Db() {}

    public static Connection connect() throws SQLException {
        // 이름이 다르면 서로 다른 메모리 DB 입니다.
        Connection c = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        try (Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE users (
                    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name  VARCHAR(50)  NOT NULL,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    age   INT          NOT NULL
                )""");
            st.execute("""
                CREATE TABLE orders (
                    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id BIGINT       NOT NULL REFERENCES users(id),
                    item    VARCHAR(50)  NOT NULL,
                    price   INT          NOT NULL
                )""");
        }
        return c;
    }
}

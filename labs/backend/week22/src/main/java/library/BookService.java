package library;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/** 메모리에 책을 보관하는 서비스. 주어진 코드이므로 고치지 않습니다. id 는 1 부터 시작합니다. */
@Service
public class BookService {
    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong();

    public List<Book> findAll() {
        List<Book> all = new ArrayList<>(books.values());
        all.sort((a, b) -> Long.compare(a.id(), b.id()));
        return all;
    }

    /** 없으면 BookNotFoundException */
    public Book get(long id) {
        Book b = books.get(id);
        if (b == null) throw new BookNotFoundException(id);
        return b;
    }

    public Book create(BookRequest req) {
        Book b = new Book(seq.incrementAndGet(), req.title(), req.author());
        books.put(b.id(), b);
        return b;
    }

    /** 없으면 BookNotFoundException */
    public Book update(long id, BookRequest req) {
        get(id);
        Book b = new Book(id, req.title(), req.author());
        books.put(id, b);
        return b;
    }

    /** 없으면 BookNotFoundException */
    public void delete(long id) {
        get(id);
        books.remove(id);
    }

    /** 테스트가 매번 깨끗한 상태로 시작하도록 비웁니다. */
    public void clear() {
        books.clear();
        seq.set(0);
    }
}


import org.example.Application;
import org.example.model.Transaction;
import org.example.model.TransactionRequest;
import org.example.model.TransactionType;
import org.example.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
class CachePerformanceTest extends BaseIntegrationTest {

    @Autowired
    private TransactionService service;

    private final TransactionRequest request =
            new TransactionRequest(BigDecimal.valueOf(999), TransactionType.INCOME, "cache test");

    @BeforeEach
    void setup() {
        service.deleteAll();
    }

    @Test
    void cacheHitRateTest() {
        Transaction created = service.create(request);
        Long targetId = created.id();

        long start1 = System.nanoTime();
        service.getById(targetId);
        long duration1 = System.nanoTime() - start1;

        long start2 = System.nanoTime();
        service.getById(targetId);
        long duration2 = System.nanoTime() - start2;

        System.out.printf("first query time cost: %d ns\ncache query time cost: %d ns\n", duration1, duration2);
        assertTrue(duration2 < duration1, "time cost should be shorter after using cache");
    }
}
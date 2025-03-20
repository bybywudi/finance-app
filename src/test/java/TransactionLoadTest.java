
import com.jayway.jsonpath.JsonPath;
import org.example.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TransactionLoadTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private static final ConcurrentLinkedQueue<Long> CREATED_IDS = new ConcurrentLinkedQueue<>();
    private static final String API_PATH = "/transactions";

    @Test
    @Order(1)
    void concurrentCreationTest() throws Exception {
        int threadCount = 200;
        int requestsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * requestsPerThread);

        IntStream.range(0, threadCount * requestsPerThread).forEach(i -> {
            executor.submit(() -> {
                try {
                    String json = String.format(
                            "{\"amount\": %.2f, \"type\": \"%s\", \"description\": \"Test %d\"}",
                            (double) (i % 100),
                            (i % 2 == 0) ? "INCOME" : "EXPENSE",
                            i
                    );

                    MvcResult result = mvc.perform(post(API_PATH)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                            .andExpect(status().isCreated())
                            .andReturn();

                    String response = result.getResponse().getContentAsString();
                    Long id = JsonPath.parse(response).read("$.id", Long.class);
                    CREATED_IDS.add(id);
                } catch (Exception e) {
                    System.err.println("Creation failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        });

        executor.shutdown();
        boolean completed = latch.await(1, TimeUnit.MINUTES);
        assertTrue(completed, "data creation failed");
        System.out.println("data record successfully created: " + CREATED_IDS.size());
    }

    @Test
    @Order(2)
    void concurrentReadTest() throws Exception {

        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> !CREATED_IDS.isEmpty());

        int threadCount = 200;
        int requestsPerThread = 50;
        AtomicInteger successCount = new AtomicInteger(0);
        Long[] idArray = CREATED_IDS.toArray(new Long[0]);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * requestsPerThread);

        long startTime = System.currentTimeMillis();

        IntStream.range(0, threadCount * requestsPerThread).forEach(i -> {
            executor.submit(() -> {
                try {
                    Long targetId = idArray[ThreadLocalRandom.current().nextInt(idArray.length)];

                    mvc.perform(get(API_PATH + "/" + targetId))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.id").value(targetId));

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Read failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        });

        executor.shutdown();
        boolean completed = latch.await(2, TimeUnit.MINUTES);
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("\n===== Concurrent Read Report =====");
        System.out.printf("Total Requests: %d\n", threadCount * requestsPerThread);
        System.out.printf("Successful Reads: %d (%.2f%%)\n",
                successCount.get(),
                successCount.get() * 100.0 / (threadCount * requestsPerThread));
        System.out.printf("Total Duration: %d ms\n", duration);
        System.out.printf("Throughput: %.2f req/s\n",
                successCount.get() * 1000.0 / duration);

        assertTrue(completed, "cost too much time");
        assertEquals(0,
                threadCount * requestsPerThread - successCount.get(),
                "some request failed");
    }
}
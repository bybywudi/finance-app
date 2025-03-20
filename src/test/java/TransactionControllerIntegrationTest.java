
import org.example.Application;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateAndRetrieveTransaction() throws Exception {
        String createJson = """
            {
                "amount": 1150.75,
                "type": "INCOME",
                "description": "工资"
            }
            """;

        String location = mvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1150.75))
                .andExpect(jsonPath("$.type").value("INCOME"));

        mvc.perform(get(API_PATH + "?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void shouldHandleInvalidRequest() throws Exception {
        String invalidJson = """
            {
                "amount": -110,
                "type": null
            }
            """;

        mvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldHandleDuplicateTransaction() throws Exception {
        String json = """
            {
                "amount": 200,
                "type": "EXPENSE",
                "description": "费用"
            }
            """;

        mvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        mvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TRANSACTION"));
    }
}
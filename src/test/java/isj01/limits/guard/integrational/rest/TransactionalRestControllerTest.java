package isj01.limits.guard.integrational.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import isj01.limits.guard.dto.TransactionCreateDto;
import isj01.limits.guard.exceptions.AmountLimitExceededException;
import isj01.limits.guard.exceptions.ConcurrentTransactionException;
import isj01.limits.guard.exceptions.LimitExceededException;
import isj01.limits.guard.integrational.TestContainersConfig;
import isj01.limits.guard.rest.TransactionRestController;
import isj01.limits.guard.service.TransactionService;

@WebMvcTest(TransactionRestController.class)
@Import(TestContainersConfig.class)
class TransactionalRestControllerTest {
    
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private TransactionService transactionService;

    @Test
    @DisplayName("201 CREATED: Успешное создание транзакции")
    void shouldCreateTransactionSuccessfully() throws Exception {
        TransactionCreateDto createDto = new TransactionCreateDto("user-123", 100L, "TRACE");
        when(transactionService.create(createDto)).thenReturn(42L);

        mockMvc.perform(post("/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$").value(42));
    }

    @Test
    @DisplayName("400 BAD REQUEST: Ошибка валидации DTO (отрицательная сумма)")
    void shouldReturnBadRequestWhenAmountIsInvalid() throws Exception {
        TransactionCreateDto createDto = new TransactionCreateDto("user-123", -100L, "TRACE");
        when(transactionService.create(createDto)).thenReturn(42L);
        mockMvc.perform(post("/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("409 Conflict: Конфликт при дублировании транзакций")
    void shouldReturnConflictWhenConcurrentTransaction() throws Exception {
        TransactionCreateDto createDto = new TransactionCreateDto("user-123", 100L, "SHOP");
        when(transactionService.create(any()))
            .thenThrow(new ConcurrentTransactionException("Дублирующий запрос"));

        mockMvc.perform(post("/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("429 TOO MANY REQUESTS: При превышении частоты запросов (Rate Limit)")
    void shouldReturnTooManyRequestsWhenRateLimitExceeded() throws Exception {
        TransactionCreateDto createDto = new TransactionCreateDto("user-123", 100L, "SHOP");
        when(transactionService.create(any()))
            .thenThrow(new LimitExceededException("Превышена частота запросов"));

        mockMvc.perform(post("/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("422 UNPROCESSABLE ENTITY: При превышении суточного лимита по сумме")
    void shouldReturnUnprocessableEntityWhenAmountLimitExceeded() throws Exception {
        TransactionCreateDto createDto = new TransactionCreateDto("user-123", 100L, "SHOP");
        when(transactionService.create(any()))
            .thenThrow(new AmountLimitExceededException("Превышен суточный лимит запросов"));

        mockMvc.perform(post("/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isUnprocessableContent());
    }

}

package isj01.limits.guard.integrational.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.Clock;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import isj01.limits.guard.dto.TransactionCreateDto;
import isj01.limits.guard.exceptions.ConcurrentTransactionException;
import isj01.limits.guard.integrational.TestContainersConfig;
import isj01.limits.guard.mapper.TransactionMapperImpl;
import isj01.limits.guard.service.TransactionDbService;
import isj01.limits.guard.service.TransactionService;

@SpringBootTest
@Import({TestContainersConfig.class, TransactionMapperImpl.class})
class TransactionServiceTest {

    @Autowired private TransactionService transactionService;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private Clock clock;

    @MockitoSpyBean private TransactionDbService dbService;

    @BeforeEach
    void setUp() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    @Test
    @DisplayName("Успешное создание транзакции: БД сохраняет, Redis инкрементит")
    void shouldCreateTransactionSuccessfully() {
        TransactionCreateDto createDto = new TransactionCreateDto("user-123", 2000L, "shop");
        
        Long id = transactionService.create(createDto);
        
        assertThat(id).isNotNull();
        
        String dailyLimitKey = "limit:user:user-123:day:" + LocalDate.now(clock);
        assertThat(redisTemplate.opsForValue().get(dailyLimitKey)).isEqualTo("2000");
    }

    @Test
    @DisplayName("Защита от дублей: блокировка срабатывает")
    void shouldThrowExceptionOnConcurrentTransaction() {
        TransactionCreateDto createDto = new TransactionCreateDto("user-123", 1000L, "shop");

        transactionService.create(createDto);

        assertThatThrownBy(() -> transactionService.create(createDto))
            .isInstanceOf(ConcurrentTransactionException.class)
            .hasMessageContaining("Дублирующий запрос");
    }

    @Test
    @DisplayName("Rollback: если БД падает, счетчики в Redis не увеличиваются")
    void shouldNotIncrementRedisWhenDbFails() {
        TransactionCreateDto createDto = new TransactionCreateDto("user-300", 500L, "shop");
        String dailyLimitKey = "limit:user:user-300:day:" + LocalDate.now(clock);

        doThrow(new RuntimeException("Database is down!"))
            .when(dbService).saveToDatabase(any(TransactionCreateDto.class));

        assertThatThrownBy(() -> transactionService.create(createDto))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database is down!");

        assertThat(redisTemplate.hasKey(dailyLimitKey)).isFalse();
    }   

}

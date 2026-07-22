package isj01.limits.guard.integrational.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import isj01.limits.guard.integrational.TestContainersConfig;
import isj01.limits.guard.service.RedisService;

@SpringBootTest
@Import(TestContainersConfig.class)
class RedisServiceTest {

    @Value("${user-transaction.day-amount-limit:5}")
    private long dailyLimit;

    @Autowired private RedisService redisService;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private Clock clock;

    @BeforeEach
    void setUp() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    @Test
    @DisplayName("Успешный захват блокировки и повторный отказ для той же транзакции")
    void testTryAcquireTxLock() {
        String userId = "user-123";
        Long amount = 100l;
        String key = "lock:tx:" + userId + ":" + amount;

        boolean result = redisService.tryAcquireTxLock(userId, amount);
        boolean result2 = redisService.tryAcquireTxLock(userId, amount);

        assertThat(result).isTrue();
        assertThat(result2).isFalse();
        assertThat(redisTemplate.getExpire(key))
            .isNotNull()
            .isGreaterThan(0);
    }

    @Test
    @DisplayName("Инкремент дневной суммы и проверка превышения лимита")
    void testAmountLimit() {
        String userId = "user-123";
        Long amount = 100L;
        String key = buildDailyLimitKey(userId);

        redisService.incrementDailyCount(userId, amount);

        assertThat(Long.parseLong(redisTemplate.opsForValue().get(key))).isEqualTo(amount);
        assertThat(redisService.isAmountLimitExceeded(userId)).isFalse();
        
        redisService.incrementDailyCount(userId, dailyLimit);
        assertThat(redisService.isAmountLimitExceeded(userId)).isTrue();
    }

    @Test
    @DisplayName("Срабатывание rate limit при превышении количества запросов")
    void testRateLimit() {
        String userId = "user-123";

        redisService.addRequestToLimits(userId);

        assertThat(redisService.isRateLimitExceeded(userId)).isFalse();
        
        redisService.addRequestToLimits(userId);
        redisService.addRequestToLimits(userId);
        
        assertThat(redisService.isRateLimitExceeded(userId)).isTrue();
    }

    private String buildDailyLimitKey(String userId) {
        return "limit:user:" + userId + ":day:" + LocalDate.now(clock);
    }

}

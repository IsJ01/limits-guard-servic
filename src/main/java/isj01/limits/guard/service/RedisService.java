package isj01.limits.guard.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {

    @Value("${user-transaction.day-amount-limit:5}")
    private long dailyLimit;
    
    @Value("${user-transaction.rate-limit:3}")
    private long rateLimit;

    private static final Duration LOCK_TTL = Duration.ofSeconds(5);
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;
    private final Clock clock;

    public boolean tryAcquireTxLock(String userId, Long amount) {
        String key = "lock:tx:" + userId + ":" + amount;
        Boolean acquire = redisTemplate.opsForValue().setIfAbsent(key, "", LOCK_TTL);
        return Boolean.TRUE.equals(acquire);
    }

    public boolean isAmountLimitExceeded(String userId) {
        String key = buildDailyLimitKey(userId);
        String rawValue = redisTemplate.opsForValue().get(key);

        if (rawValue == null)
            return false;
        try {
            return Long.parseLong(rawValue) >= dailyLimit;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void incrementDailyCount(String userId, Long amount) {
        String key = buildDailyLimitKey(userId);
        redisTemplate.opsForValue().increment(key, amount);

        Long expire = redisTemplate.getExpire(key);
        if (expire != null && expire < 0) {
            redisTemplate.expire(key, getDurationUntilNextDay());
        }
    }

    public boolean isRateLimitExceeded(String userId) {
        String key = buildRateLimitKey(userId);
        long windowStart = Instant.now(clock).toEpochMilli() - RATE_LIMIT_WINDOW.toMillis();
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        Long currentRequests = redisTemplate.opsForZSet().zCard(key);
        return currentRequests != null && currentRequests >= rateLimit;
    }

    public void addRequestToLimits(String userId) {
        String key = buildRateLimitKey(userId);

        long now = Instant.now(clock).toEpochMilli();
        String value = now + ":" + UUID.randomUUID();
        redisTemplate.opsForZSet().add(key, value, now);

        redisTemplate.expire(key, RATE_LIMIT_WINDOW.plusSeconds(10));
    }

    private String buildRateLimitKey(String userId) {
        return "ratelimit:user:" + userId;
    }

    private String buildDailyLimitKey(String userId) {
        return "limit:user:" + userId + ":day:" + LocalDate.now(clock);
    }

    private Duration getDurationUntilNextDay() {
        ZonedDateTime now = ZonedDateTime.now(clock);
        ZonedDateTime nextDayStart = now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
        return Duration.between(now, nextDayStart);
    }

}

package isj01.limits.guard.service;

import org.springframework.stereotype.Service;
import isj01.limits.guard.dto.TransactionCreateDto;
import isj01.limits.guard.exceptions.AmountLimitExceededException;
import isj01.limits.guard.exceptions.ConcurrentTransactionException;
import isj01.limits.guard.exceptions.LimitExceededException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionDbService dbService;
    private final RedisService redisService;

    public Long create(TransactionCreateDto createDto) {
        if (!redisService.tryAcquireTxLock(createDto.getUserId(), createDto.getAmount())) {
            throw new ConcurrentTransactionException("Дублирующий запрос");
        }
        if (redisService.isRateLimitExceeded(createDto.getUserId())) {
            throw new LimitExceededException("Превышена частота запросов");
        }
        if (redisService.isAmountLimitExceeded(createDto.getUserId())) {
            throw new AmountLimitExceededException("Превышен суточный лимит запросов");
        }

        Long id = dbService.saveToDatabase(createDto);

        redisService.incrementDailyCount(createDto.getUserId(), createDto.getAmount());
        redisService.addRequestToLimits(createDto.getUserId());

        return id;
    }

}

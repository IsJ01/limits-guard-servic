package isj01.limits.guard.dto;

import lombok.Value;

@Value
public class TransactionCreateDto {
    String userId;
    Long amount;
    String merchantId;
}

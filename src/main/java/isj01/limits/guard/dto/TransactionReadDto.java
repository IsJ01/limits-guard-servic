package isj01.limits.guard.dto;

import lombok.Value;

@Value
public class TransactionReadDto {
    Long id;
    String userId;
    Long amount;
    String merchantId;
}

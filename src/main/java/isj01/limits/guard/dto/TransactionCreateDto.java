package isj01.limits.guard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransactionCreateDto (
    @NotBlank String userId, 
    @Positive Long amount, 
    @NotBlank String merchantId) {
}

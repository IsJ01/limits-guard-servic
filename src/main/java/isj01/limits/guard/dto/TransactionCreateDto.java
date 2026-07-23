package isj01.limits.guard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransactionCreateDto (
    @JsonProperty("user_id") @NotBlank String userId, 
    @Positive Long amount, 
    @JsonProperty("merchant_id")@NotBlank String merchantId) {
}

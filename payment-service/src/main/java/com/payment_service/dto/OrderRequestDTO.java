package com.payment_service.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {

    // Populated from X-User-Id gateway header if not in body
    private String userId;

    // Populated from X-User-Email gateway header if not in body
    private String email;

    @NotNull
    @DecimalMin(value = "1")
    private BigDecimal amount;

    @NotBlank
    private String currency;

    // Optional — plan name for display
    private String templateId;
}

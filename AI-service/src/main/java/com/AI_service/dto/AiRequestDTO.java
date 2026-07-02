package com.AI_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRequestDTO {
    
    @NotNull(message = "User ID must not be null")
    private Long userId;
    
    @NotBlank(message = "Prompt must not be blank")
    private String prompt;
}

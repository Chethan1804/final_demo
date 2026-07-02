package com.Userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRequestDTO {

    private Long userId;

    @NotBlank(message = "Action cannot be blank")
    private String action;

    private String entityType;
    private Long entityId;
    private String details;
}

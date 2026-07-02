package com.AI_service.dto;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuggestionResponse {
    private String strengths;
    private String weaknesses;
    private String improvements;
}

package com.mss.user_service.payloads.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCompletionResponse {
    private Boolean completed;
    private Double percentage;
    private List<String> missingFields;
}


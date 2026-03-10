package com.mss.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesDto {
    private String language;
    private String currency;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean promotionalEmails;
}


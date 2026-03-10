package com.mss.user_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {

    @Builder.Default
    private String language = "vi";

    @Builder.Default
    private String currency = "VND";

    @Builder.Default
    private Boolean emailNotifications = true;

    @Builder.Default
    private Boolean smsNotifications = false;

    @Builder.Default
    private Boolean promotionalEmails = true;

    public static UserPreferences createDefault() {
        return UserPreferences.builder()
                .language("vi")
                .currency("VND")
                .emailNotifications(true)
                .smsNotifications(false)
                .promotionalEmails(true)
                .build();
    }
}


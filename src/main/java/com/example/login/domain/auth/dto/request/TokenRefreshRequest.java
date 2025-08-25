package com.example.login.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 재발급 요청")
public class TokenRefreshRequest {
    
    @Schema(description = "재발급 타입", example = "ACCESS_ONLY", allowableValues = {"ACCESS_ONLY", "FULL_REFRESH"})
    private String refreshType;
    
    public enum RefreshType {
        ACCESS_ONLY,    // Access Token만 재발급
        FULL_REFRESH    // Access + Refresh Token 모두 재발급
    }
    
    public RefreshType getRefreshTypeEnum() {
        try {
            return RefreshType.valueOf(refreshType);
        } catch (IllegalArgumentException e) {
            return RefreshType.ACCESS_ONLY; // 기본값
        }
    }
}
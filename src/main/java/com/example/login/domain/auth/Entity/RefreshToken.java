package com.example.login.domain.auth.Entity;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.annotation.Id;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value = "refreshToken", timeToLive = 1209600)
public class RefreshToken {

    @Id
    private String memberId;

    private String token;

    public RefreshToken update(String newToken) {
        return RefreshToken.builder()
                .memberId(this.memberId)
                .token(newToken)
                .build();
    }
}

package com.example.login.Refresh.Entity;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.annotation.Id;

@RedisHash(value = "refreshToken", timeToLive = 1209600) // 14일 TTL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    private String memberId;

    private String token;
}

package com.jinsu.ticketrace.auth.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class RedisAccessTokenBlacklistStore implements AccessTokenBlacklistStore {

    private final StringRedisTemplate redis;

    private String key(String accessTokenValue) {
        return "auth:jwt:access:blacklist:" + accessTokenValue;
    }

    @Override
    public void blacklist(String accessTokenValue, Duration ttl) {
        redis.opsForValue().set(key(accessTokenValue),"1",ttl);
    }

    @Override
    public boolean isBlacklisted(String accessTokenValue) {
        return redis.hasKey(key(accessTokenValue));
    }
}

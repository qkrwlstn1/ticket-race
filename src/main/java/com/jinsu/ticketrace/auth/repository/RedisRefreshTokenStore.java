package com.jinsu.ticketrace.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private final StringRedisTemplate redis;

    private String key(long memberPk) {
        return "auth:jwt:refresh:" + memberPk;
    }

    @Override
    public void save(long memberPk, String refreshTokenValue, Duration ttl) {
        redis.opsForValue().set(key(memberPk), refreshTokenValue, ttl);
    }

    @Override
    public Optional<String> find(long memberPk) {
        return Optional.ofNullable(
                redis.
                        opsForValue().
                        get(key(memberPk))
        );
    }

    @Override
    public void delete(long memberPk) {
        redis.delete(key(memberPk));
    }
}

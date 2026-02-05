package com.jinsu.ticketrace.auth.repository.redis.integration;

import com.jinsu.ticketrace.auth.repository.redis.AccessTokenBlacklistStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RedisAccessTokenBlacklistStoreTest {
    @Container
    @ServiceConnection
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7.2.5")
            .withExposedPorts(6379);

    @Autowired
    private AccessTokenBlacklistStore accessTokenBlacklistStore;
    @Test
    @DisplayName("Access token 블랙리스트가 Redis에 기록된다")
    void accessToken_Blacklist_Persisted() {
        //given
        String accessToken = "access-token";
        //when
        accessTokenBlacklistStore.blacklist(accessToken, Duration.ofMinutes(10));
        //then
        assertTrue(accessTokenBlacklistStore.isBlacklisted(accessToken));

    }
}

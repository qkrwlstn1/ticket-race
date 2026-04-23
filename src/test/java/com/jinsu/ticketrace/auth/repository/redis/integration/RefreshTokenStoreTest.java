package com.jinsu.ticketrace.auth.repository.redis.integration;

import com.jinsu.ticketrace.auth.repository.redis.RefreshTokenStore;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class RefreshTokenStoreTest {
    @Container
    @ServiceConnection
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7.2.5")
            .withExposedPorts(6379);

    @Autowired
    private RefreshTokenStore refreshTokenStore;

    @Test
    @DisplayName("Refresh token 저장/조회/삭제가 Redis에 반영된다")
    void refreshToken_SaveFindDelete_Persisted() {
        //given
        long memberPk = 1L;

        //when
        refreshTokenStore.save(memberPk, "refresh-token", Duration.ofMinutes(10));
        String refreshToken = refreshTokenStore.find(memberPk).orElseThrow();
        refreshTokenStore.delete(memberPk);

        assertEquals("refresh-token", refreshToken);
        assertTrue(refreshTokenStore.find(memberPk).isEmpty());


    }

}

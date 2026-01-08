package com.jinsu.ticketrace.auth.repository.redis;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenStore {
    void save(long memberPk, String refreshTokenValue, Duration ttl);
    Optional<String> find(long memberPk);
    void delete(long memberPk);
}

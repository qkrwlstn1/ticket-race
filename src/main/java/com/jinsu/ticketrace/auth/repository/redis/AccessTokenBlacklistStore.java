package com.jinsu.ticketrace.auth.repository.redis;

import java.time.Duration;

public interface AccessTokenBlacklistStore {
    void blacklist(String accessTokenValue, Duration ttl);
    boolean isBlacklisted(String accessTokenValue);
}

package support.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    // Redis Testcontainer 정의
    @Bean
    @ServiceConnection(name = "redisContainer") // 적절한 이름을 추가합니다.
    public RedisContainer redisContainer() {
        return new RedisContainer("redis:latest") // 최신 또는 요구되는 Redis 버전을 사용하세요.
                .withExposedPorts(6379); // Redis 기본 포트를 엽니다.
    }
}

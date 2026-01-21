package support.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration(proxyBeanMethods = false)
public class RedisConfig {

    @Bean
    @ServiceConnection
    GenericContainer<?> redisContainer(){
        return  new GenericContainer<>("redis:7.2.5")
                .withExposedPorts(6379);
    }

}

package com.jinsu.ticketrace.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

@Configuration
public class TicketSaleRedisConfig {

    /**
     * 재고/잔액 선점 + queue 적재
     * KEYS[1] = quantity key
     * KEYS[2] = account key
     * KEYS[3] = queue key
     *
     * ARGV[1] = amount
     * ARGV[2] = totalPrice
     * ARGV[3] = payload
     */
    @Bean
    public DefaultRedisScript<Long> reserveTicketSaleScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("""
                if redis.call('EXISTS', KEYS[1]) == 0 or redis.call('EXISTS', KEYS[2]) == 0 then
                    return -2
                end

                local amount = tonumber(ARGV[1])
                local totalPrice = tonumber(ARGV[2])
                local payload = ARGV[3]

                local quantity = tonumber(redis.call('GET', KEYS[1]))
                if quantity < amount then
                    return -1
                end

                local balance = tonumber(redis.call('GET', KEYS[2]))
                if balance < totalPrice then
                    return -3
                end

                redis.call('DECRBY', KEYS[1], amount)
                redis.call('DECRBY', KEYS[2], totalPrice)
                redis.call('LPUSH', KEYS[3], payload)

                return 1
                """);
        script.setResultType(Long.class);
        return script;
    }

    /**
     * queue -> processing 으로 최대 N개 이동
     * KEYS[1] = queue key
     * KEYS[2] = processing key
     *
     * ARGV[1] = batch size
     *
     * return = claimed payload list
     */
    @Bean
    public DefaultRedisScript<List> batchClaimScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText("""
                local queueKey = KEYS[1]
                local processingKey = KEYS[2]
                local batchSize = tonumber(ARGV[1])

                local result = {}

                for i = 1, batchSize do
                    local payload = redis.call('RPOPLPUSH', queueKey, processingKey)
                    if not payload then
                        break
                    end
                    table.insert(result, payload)
                end

                return result
                """);
        script.setResultType(List.class);
        return script;
    }

    /**
     * processing 에서 제거 후 queue 로 복귀
     * KEYS[1] = queue key
     * KEYS[2] = processing key
     *
     * ARGV = payloads...
     *
     * return = 실제로 복귀시킨 개수
     */
    @Bean
    public DefaultRedisScript<Long> batchRequeueScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("""
                local queueKey = KEYS[1]
                local processingKey = KEYS[2]
                local moved = 0

                for i = 1, #ARGV do
                    local payload = ARGV[i]
                    local removed = redis.call('LREM', processingKey, 1, payload)
                    if removed == 1 then
                        redis.call('RPUSH', queueKey, payload)
                        moved = moved + 1
                    end
                end

                return moved
                """);
        script.setResultType(Long.class);
        return script;
    }
}
package com.jinsu.ticketrace.ticket.sale.repository.redis;

import com.jinsu.ticketrace.ticket.sale.service.TicketSaleCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisTicketSaleRequestStore implements TicketSaleRequestStore {

    private static final String QUEUE_KEY = "ticket:sale:queue";
    private static final String PROCESSING_KEY = "ticket:sale:processing";

    private final StringRedisTemplate redisTemplate;

    @Qualifier("reserveTicketSaleScript")
    private final DefaultRedisScript<Long> reserveTicketSaleScript;

    @Qualifier("batchClaimScript")
    private final DefaultRedisScript<List> batchClaimScript;

    @Qualifier("batchRequeueScript")
    private final DefaultRedisScript<Long> batchRequeueScript;

    @Override
    public TicketSaleReserveResult reserve(TicketSaleCommand command) {
        String quantityKey = quantityKey(command.boardPk());
        String accountKey = accountKey(command.memberPk());
        String payload = command.serialize();

        Long result = redisTemplate.execute(
                reserveTicketSaleScript,
                List.of(quantityKey, accountKey, QUEUE_KEY),
                String.valueOf(command.amount()),
                String.valueOf(command.totalPrice()),
                payload
        );

        return TicketSaleReserveResult.fromCode(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> claimUpTo(int batchSize) {
        if (batchSize <= 0) {
            return Collections.emptyList();
        }

        List<String> result = redisTemplate.execute(
                batchClaimScript,
                List.of(QUEUE_KEY, PROCESSING_KEY),
                String.valueOf(batchSize)
        );

        return result == null ? Collections.emptyList() : result;
    }

    @Override
    public void acknowledgeAll(List<String> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return;
        }

        var serializer = redisTemplate.getStringSerializer();

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            byte[] processingKey = serializer.serialize(PROCESSING_KEY);

            for (String payload : payloads) {
                if (payload == null) {
                    continue;
                }
                byte[] value = serializer.serialize(payload);
                connection.listCommands().lRem(processingKey, 1, value);
            }
            return null;
        });
    }

    @Override
    public long requeueAll(List<String> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return 0L;
        }

        Long moved = redisTemplate.execute(
                batchRequeueScript,
                List.of(QUEUE_KEY, PROCESSING_KEY),
                (Object[]) payloads.toArray(String[]::new)
        );

        return moved == null ? 0L : moved;
    }

    private String quantityKey(long boardPk) {
        return "ticket:inventory:ga:" + boardPk + ":quantity";
    }

    private String accountKey(long memberPk) {
        return "ticket:member:" + memberPk + ":account";
    }
}
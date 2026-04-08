package com.jinsu.ticketrace.ticket.sale.service;

import com.jinsu.ticketrace.ticket.sale.repository.redis.TicketSaleRequestStore;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketSalePersistenceWorker {

    private final TicketSaleRequestStore ticketSaleRequestStore;
    private final TicketSalePersistenceService ticketSalePersistenceService;

    @Scheduled(fixedDelay = 200L)
    public void drainQueue() {
        while (true) {
            List<String> payloads = ticketSaleRequestStore.claimUpTo(100);
            if (payloads.isEmpty()) {
                return;
            }

            List<TicketSaleCommand> commands = payloads.stream()
                    .map(TicketSaleCommand::deserialize)
                    .toList();

            try {
                ticketSalePersistenceService.persistBatch(commands);
                ticketSaleRequestStore.acknowledgeAll(payloads);
            } catch (RuntimeException exception) {
                ticketSaleRequestStore.requeueAll(payloads);
                return;
            }
        }
    }
}
package com.jinsu.ticketrace.ticket.sale.service;

import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import com.jinsu.ticketrace.ticket.sale.domain.entity.Ticket;
import com.jinsu.ticketrace.ticket.sale.repository.TicketRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketSalePersistenceService {

    private final TicketBoardRepository ticketBoardRepository;
    private final MemberRepository memberRepository;
    private final TicketRepository ticketRepository;
    private final EntityManager entityManager;

    @Transactional
    public void persistBatch(List<TicketSaleCommand> commands) {
        if (commands.isEmpty()) {
            return;
        }

        // 1) 요청 ID 중복 제거
        List<String> requestIds = commands.stream()
                .map(TicketSaleCommand::requestId)
                .toList();

        List<String> existingRequestIds = ticketRepository.findExistingRequestIds(requestIds);

        List<TicketSaleCommand> filtered = commands.stream()
                .filter(command -> !existingRequestIds.contains(command.requestId()))
                .toList();

        if (filtered.isEmpty()) {
            return;
        }

        // 2) board별 총 감소량 집계
        Map<Long, Long> boardAmountMap = new HashMap<>();
        for (TicketSaleCommand command : filtered) {
            boardAmountMap.merge(command.boardPk(), command.amount(), Long::sum);
        }

        // 3) member별 총 차감액 집계
        Map<Long, Long> memberPriceMap = new HashMap<>();
        for (TicketSaleCommand command : filtered) {
            memberPriceMap.merge(command.memberPk(), command.totalPrice(), Long::sum);
        }

        // 4) 재고 차감
        for (Map.Entry<Long, Long> entry : boardAmountMap.entrySet()) {
            int updated = ticketBoardRepository.decreaseQuantity(entry.getKey(), entry.getValue());
            if (updated != 1) {
                throw new IllegalStateException("board decrease failed. boardPk=" + entry.getKey());
            }
        }

        // 5) 잔액 차감
        for (Map.Entry<Long, Long> entry : memberPriceMap.entrySet()) {
            int updated = memberRepository.decreaseAccount(entry.getKey(), entry.getValue());
            if (updated != 1) {
                throw new IllegalStateException("member decrease failed. memberPk=" + entry.getKey());
            }
        }

        // 6) 티켓 insert
        List<Ticket> tickets = filtered.stream()
                .map(command -> Ticket.of(
                        command.requestId(),
                        ticketBoardRepository.getReferenceById(command.boardPk()),
                        memberRepository.getReferenceById(command.memberPk()),
                        command.amount(),
                        command.totalPrice()
                ))
                .toList();

        ticketRepository.saveAll(tickets);

        entityManager.flush();
        entityManager.clear();
    }
}
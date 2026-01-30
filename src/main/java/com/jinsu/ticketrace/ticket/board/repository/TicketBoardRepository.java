package com.jinsu.ticketrace.ticket.board.repository;

import com.jinsu.ticketrace.ticket.board.domain.entity.TicketBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TicketBoardRepository extends JpaRepository<TicketBoard, Long> {

    @Query("select tb from " +
            "TicketBoard tb left join fetch tb.member " +
            "where tb.boardPk = :id ")
    Optional<TicketBoard> findTicketBoardEager(long id);

}

package com.jinsu.ticketrace.ticket.board.repository;

import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TicketBoardRepository extends JpaRepository<GATicketBoard, Long> {

    @Query("select tb from " +
            "GATicketBoard tb left join fetch tb.member " +
            "where tb.boardPk = :id ")
    Optional<GATicketBoard> findTicketBoardEager(long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select tb from from GATicketBoard tb where tb.boardPk = :id")
    Optional<GATicketBoard> findByWithPessimisticLock(long id);


    @Modifying
    @Query("""
            update GATicketBoard tb 
            set tb.quantity = tb.quantity - :amount 
            where tb.quantity >= :amount
                and tb.boardPk = :ticketBoardPk
            """)
    int decreaseQuantity(long ticketBoardPk, long amount);

    Optional<Long> findPriceByBoardPk(long boardPk);

}

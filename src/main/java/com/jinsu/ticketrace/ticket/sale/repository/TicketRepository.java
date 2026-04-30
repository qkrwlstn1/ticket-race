package com.jinsu.ticketrace.ticket.sale.repository;

import com.jinsu.ticketrace.ticket.sale.domain.entity.Ticket;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    boolean existsByBuyRequestId(String buyRequestId);

    @Query("""
        select t.buyRequestId
        from Ticket t
        where t.buyRequestId in :requestIds
    """)
    List<String> findExistingRequestIds(@Param("requestIds") List<String> requestIds);
}

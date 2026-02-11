package com.jinsu.ticketrace.ticket.sale.repository;

import com.jinsu.ticketrace.ticket.sale.domain.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
}

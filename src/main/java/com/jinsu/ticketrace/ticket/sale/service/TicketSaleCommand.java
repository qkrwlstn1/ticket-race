package com.jinsu.ticketrace.ticket.sale.service;

public record TicketSaleCommand(
        String requestId,
        long boardPk,
        long memberPk,
        long amount,
        long price
) {
    public String serialize() {
        return String.join(":",
                requestId,
                String.valueOf(boardPk),
                String.valueOf(memberPk),
                String.valueOf(amount),
                String.valueOf(price)
        );
    }

    public long totalPrice() {
        return price * amount;
    }

    public static TicketSaleCommand deserialize(String value) {
        String[] tokens = value.split(":");
        if (tokens.length != 5) {
            throw new IllegalArgumentException("invalid ticket sale command payload");
        }
        return new TicketSaleCommand(
                tokens[0],
                Long.parseLong(tokens[1]),
                Long.parseLong(tokens[2]),
                Long.parseLong(tokens[3]),
                Long.parseLong(tokens[4])
        );
    }
}
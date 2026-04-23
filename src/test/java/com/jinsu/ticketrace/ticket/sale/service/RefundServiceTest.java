package com.jinsu.ticketrace.ticket.sale.service;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.TicketErrorCode;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import com.jinsu.ticketrace.ticket.sale.domain.entity.Ticket;
import com.jinsu.ticketrace.ticket.sale.repository.TicketRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import support.config.RedisConfig;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(RedisConfig.class)
class RefundServiceTest {

    @Autowired private RefundService refundService;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private TicketBoardRepository ticketBoardRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        ticketRepository.deleteAll();
        ticketBoardRepository.deleteAll();
        memberRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    // ── 정상 환불 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Redis 키가 있을 때 환불하면 DB와 Redis가 모두 복구된다")
    void refund_성공_Redis키있을때_DB와Redis모두복구() {
        Member seller = saveMember("seller", 0);
        Member buyer  = saveMember("buyer", 0);
        GATicketBoard board  = saveBoard(seller, 5, 10_000);
        Ticket ticket = saveTicket(board, buyer, 2, 10_000);  // refundedPrice=20_000

        setRedisInventory(board.getBoardPk(), 3);
        setRedisAccount(buyer.getMemberPk(), 50_000);

        refundService.refund(ticket.getTicketPk(), buyer.getMemberPk());

        assertEquals(7,        ticketBoardRepository.findById(board.getBoardPk()).orElseThrow().getQuantity());
        assertEquals(20_000,   memberRepository.findById(buyer.getMemberPk()).orElseThrow().getAccount());
        assertEquals("5",      getRedisInventory(board.getBoardPk()));   // 3+2
        assertEquals("70000",  getRedisAccount(buyer.getMemberPk()));    // 50_000+20_000
    }

    @Test
    @DisplayName("Redis 키가 없을 때 환불하면 DB만 복구되고 Redis 키는 생성되지 않는다")
    void refund_성공_Redis키없을때_DB만복구() {
        Member seller = saveMember("seller", 0);
        Member buyer  = saveMember("buyer", 0);
        GATicketBoard board  = saveBoard(seller, 5, 10_000);
        Ticket ticket = saveTicket(board, buyer, 1, 10_000);

        refundService.refund(ticket.getTicketPk(), buyer.getMemberPk());

        assertEquals(6,      ticketBoardRepository.findById(board.getBoardPk()).orElseThrow().getQuantity());
        assertEquals(10_000, memberRepository.findById(buyer.getMemberPk()).orElseThrow().getAccount());
        assertNull(getRedisInventory(board.getBoardPk()));  // 키 미생성
        assertNull(getRedisAccount(buyer.getMemberPk()));
    }

    // ── 예외 케이스 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("이미 환불된 티켓을 다시 환불하면 ALREADY_REFUNDED 예외가 발생한다")
    void refund_중복환불_예외() {
        Member seller = saveMember("seller", 0);
        Member buyer  = saveMember("buyer", 0);
        GATicketBoard board  = saveBoard(seller, 5, 10_000);
        Ticket ticket = saveTicket(board, buyer, 1, 10_000);

        refundService.refund(ticket.getTicketPk(), buyer.getMemberPk());

        GlobalException ex = assertThrows(GlobalException.class,
                () -> refundService.refund(ticket.getTicketPk(), buyer.getMemberPk()));
        assertEquals(TicketErrorCode.ALREADY_REFUNDED, ex.getErrorCode());
    }

    @Test
    @DisplayName("본인 소유가 아닌 티켓을 환불하면 TICKET_NOT_OWNER 예외가 발생한다")
    void refund_타인티켓_예외() {
        Member seller   = saveMember("seller", 0);
        Member buyer    = saveMember("buyer", 0);
        Member stranger = saveMember("stranger", 0);
        GATicketBoard board  = saveBoard(seller, 5, 10_000);
        Ticket ticket = saveTicket(board, buyer, 1, 10_000);

        GlobalException ex = assertThrows(GlobalException.class,
                () -> refundService.refund(ticket.getTicketPk(), stranger.getMemberPk()));
        assertEquals(TicketErrorCode.TICKET_NOT_OWNER, ex.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 티켓을 환불하면 TICKET_NOT_FOUND 예외가 발생한다")
    void refund_존재하지않는티켓_예외() {
        Member buyer = saveMember("buyer", 0);

        GlobalException ex = assertThrows(GlobalException.class,
                () -> refundService.refund(99999L, buyer.getMemberPk()));
        assertEquals(TicketErrorCode.TICKET_NOT_FOUND, ex.getErrorCode());
    }

    // ── Lua 스크립트 원자성 검증 ─────────────────────────────────────────

    @Test
    @DisplayName("환불 직전 Redis 키가 만료되어도 DB 복구는 성공하고 Redis 키는 생성되지 않는다")
    void refund_Redis키만료시_DB복구성공() {
        Member seller = saveMember("seller", 0);
        Member buyer  = saveMember("buyer", 0);
        GATicketBoard board  = saveBoard(seller, 10, 5_000);
        Ticket ticket = saveTicket(board, buyer, 1, 5_000);

        setRedisInventory(board.getBoardPk(), 9);
        setRedisAccount(buyer.getMemberPk(), 95_000);
        redisTemplate.delete("ticket:inventory:ga:" + board.getBoardPk() + ":quantity");
        redisTemplate.delete("ticket:member:" + buyer.getMemberPk() + ":account");

        refundService.refund(ticket.getTicketPk(), buyer.getMemberPk());

        assertEquals(11,    ticketBoardRepository.findById(board.getBoardPk()).orElseThrow().getQuantity());
        assertEquals(5_000, memberRepository.findById(buyer.getMemberPk()).orElseThrow().getAccount());
        assertNull(getRedisInventory(board.getBoardPk()));
        assertNull(getRedisAccount(buyer.getMemberPk()));
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────

    private Member saveMember(String id, long account) {
        return memberRepository.save(Member.builder()
                .memberId(id).email(id + "@test.com").password("pw").nickname(id).account(account).build());
    }

    private GATicketBoard saveBoard(Member member, long qty, long price) {
        return ticketBoardRepository.save(GATicketBoard.builder()
                .title("concert").content("open run").quantity(qty).price(price)
                .deadlineDateTime(LocalDateTime.now().plusDays(1)).member(member).build());
    }

    private Ticket saveTicket(GATicketBoard board, Member member, long amount, long unitPrice) {
        return ticketRepository.save(
                Ticket.of(UUID.randomUUID().toString(), board, member, amount, unitPrice * amount));
    }

    private void setRedisInventory(long boardPk, long qty) {
        redisTemplate.opsForValue().set("ticket:inventory:ga:" + boardPk + ":quantity", String.valueOf(qty));
    }
    private void setRedisAccount(long memberPk, long account) {
        redisTemplate.opsForValue().set("ticket:member:" + memberPk + ":account", String.valueOf(account));
    }
    private String getRedisInventory(long boardPk) {
        return redisTemplate.opsForValue().get("ticket:inventory:ga:" + boardPk + ":quantity");
    }
    private String getRedisAccount(long memberPk) {
        return redisTemplate.opsForValue().get("ticket:member:" + memberPk + ":account");
    }
}

package com.jinsu.ticketrace.ticket.sale.service;

import static org.junit.jupiter.api.Assertions.*;
import com.jinsu.ticketrace.global.error.GlobalException;
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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "spring.task.scheduling.enabled=false")
@ActiveProfiles("test")
@Import(RedisConfig.class)
class RedisTicketSaleServiceTest {
    @Autowired
    private TicketSaleService ticketSaleService;

    @Autowired
    private TicketSalePersistenceWorker ticketSalePersistenceWorker;

    @Autowired
    private TicketBoardRepository ticketBoardRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        ticketRepository.deleteAll();
        ticketBoardRepository.deleteAll();
        memberRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    @DisplayName("판매 요청은 Redis에서 먼저 처리되고 워커가 후속 영속화를 수행한다")
    void ticketSale_success() {
        Member seller = saveMember("seller", 0);
        Member buyer = saveMember("buyer", 100_000);
        GATicketBoard board = saveBoard(seller, 10, 5_000);

        ticketSaleService.ticketSale(board.getBoardPk(), buyer.getMemberPk(), 2);

        GATicketBoard beforeFlushBoard = ticketBoardRepository.findById(board.getBoardPk()).orElseThrow();
        Member beforeFlushBuyer = memberRepository.findById(buyer.getMemberPk()).orElseThrow();

        assertEquals(10, beforeFlushBoard.getQuantity());
        assertEquals(100_000, beforeFlushBuyer.getAccount());
        assertEquals(0, ticketRepository.count());
        assertEquals("8", redisQuantity(board.getBoardPk()));
        assertEquals("90000", redisAccount(buyer.getMemberPk()));

        ticketSalePersistenceWorker.drainQueue();

        GATicketBoard savedBoard = ticketBoardRepository.findById(board.getBoardPk()).orElseThrow();
        Member savedBuyer = memberRepository.findById(buyer.getMemberPk()).orElseThrow();

        assertEquals(8, savedBoard.getQuantity());
        assertEquals(90_000, savedBuyer.getAccount());
        assertEquals(1, ticketRepository.count());
    }

    @Test
    @DisplayName("잔액 부족이면 Redis와 DB 모두 차감되지 않고 큐에도 적재되지 않는다")
    void ticketSale_releaseRedisStockWhenPaymentFails() {
        Member seller = saveMember("seller", 0);
        Member buyer = saveMember("buyer", 1_000);
        GATicketBoard board = saveBoard(seller, 10, 5_000);

        assertThrows(GlobalException.class,
                () -> ticketSaleService.ticketSale(board.getBoardPk(), buyer.getMemberPk(), 1));

        ticketSalePersistenceWorker.drainQueue();

        GATicketBoard savedBoard = ticketBoardRepository.findById(board.getBoardPk()).orElseThrow();
        Member savedBuyer = memberRepository.findById(buyer.getMemberPk()).orElseThrow();

        assertEquals(10, savedBoard.getQuantity());
        assertEquals(1_000, savedBuyer.getAccount());
        assertEquals(0, ticketRepository.count());
        assertEquals("10", redisQuantity(board.getBoardPk()));
        assertEquals("1000", redisAccount(buyer.getMemberPk()));
        assertEquals(0L, queueSize());
    }


    @Test
    @DisplayName("Redis 키가 사라져도 DB 스냅샷으로 다시 채운 뒤 판매를 계속 처리한다")
    void ticketSale_rebuildRedisSnapshotWhenKeyMissing() {
        Member seller = saveMember("seller", 0);
        Member buyer = saveMember("buyer", 100_000);
        GATicketBoard board = saveBoard(seller, 10, 5_000);

        ticketSaleService.ticketSale(board.getBoardPk(), buyer.getMemberPk(), 1);
        ticketSalePersistenceWorker.drainQueue();
        redisTemplate.delete("ticket:inventory:ga:" + board.getBoardPk() + ":quantity");
        redisTemplate.delete("ticket:member:" + buyer.getMemberPk() + ":account");

        ticketSaleService.ticketSale(board.getBoardPk(), buyer.getMemberPk(), 2);

        assertEquals("7", redisQuantity(board.getBoardPk()));
        assertEquals("85000", redisAccount(buyer.getMemberPk()));

        ticketSalePersistenceWorker.drainQueue();

        GATicketBoard savedBoard = ticketBoardRepository.findById(board.getBoardPk()).orElseThrow();
        Member savedBuyer = memberRepository.findById(buyer.getMemberPk()).orElseThrow();

        assertEquals(7, savedBoard.getQuantity());
        assertEquals(85_000, savedBuyer.getAccount());
        assertEquals(2, ticketRepository.count());
    }
    @Test
    @DisplayName("1만 동시 요청에서도 오버셀 없이 선점되고 최종 상태가 일관된다")
    void ticketSale_concurrency_10000Requests() throws Exception {
        Member seller = saveMember("seller-10k", 0);
        Member buyer = saveMember("buyer-10k", 20_000_000);
        GATicketBoard board = saveBoard(seller, 2_500, 1_000);

        initializeRedisSnapshot(board, buyer);

        int requestCount = 10_000;
        int poolSize = 200;

        ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
        CountDownLatch ready = new CountDownLatch(poolSize); // 10000 아님
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(requestCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger unexpectedFailureCount = new AtomicInteger();

        for (int i = 0; i < requestCount; i++) {
            executorService.submit(() -> {
                try {
                    ready.countDown();
                    start.await();

                    ticketSaleService.ticketSale(board.getBoardPk(), buyer.getMemberPk(), 1);
                    successCount.incrementAndGet();

                } catch (GlobalException ignored) {
                    // sold out
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException e) {
                    unexpectedFailureCount.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(ready.await(10, TimeUnit.SECONDS));
        start.countDown();
        assertTrue(done.await(60, TimeUnit.SECONDS));
        executorService.shutdownNow();

        ticketSalePersistenceWorker.drainQueue();

        GATicketBoard savedBoard = ticketBoardRepository.findById(board.getBoardPk()).orElseThrow();
        Member savedBuyer = memberRepository.findById(buyer.getMemberPk()).orElseThrow();

        assertEquals(2_500, successCount.get());
        assertEquals(0, unexpectedFailureCount.get());
        assertEquals("0", redisQuantity(board.getBoardPk()));
        assertEquals("17500000", redisAccount(buyer.getMemberPk()));

        assertEquals(0, savedBoard.getQuantity());
        assertEquals(17_500_000, savedBuyer.getAccount());
        assertEquals(2_500, ticketRepository.count());
        assertEquals(0L, queueSize());
    }

    @Test
    @DisplayName("동시 요청은 Redis에서 선점되고 워커가 순차 영속화해 오버셀 없이 반영된다")
    void ticketSale_concurrency() throws InterruptedException {
        Member seller = saveMember("seller", 0);
        Member buyer = saveMember("buyer", 1_000_000);
        GATicketBoard board = saveBoard(seller, 10, 1_000);

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch ready = new CountDownLatch(50);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(50);
        AtomicInteger successCount = new AtomicInteger();

        for (int index = 0; index < 50; index++) {
            executorService.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    ticketSaleService.ticketSale(board.getBoardPk(), buyer.getMemberPk(), 1);
                    successCount.incrementAndGet();
                } catch (GlobalException ignored) {
                    // sold out
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        executorService.shutdownNow();
        List<Ticket> tickets = ticketRepository.findAll();
        for (Ticket ticket : tickets) {
            System.out.println(ticket.getTicketPk());
        }
        assertEquals(10, successCount.get());
        assertEquals("0", redisQuantity(board.getBoardPk()));
        assertEquals("990000", redisAccount(buyer.getMemberPk()));

        ticketSalePersistenceWorker.drainQueue();

        GATicketBoard savedBoard = ticketBoardRepository.findById(board.getBoardPk()).orElseThrow();
        Member savedBuyer = memberRepository.findById(buyer.getMemberPk()).orElseThrow();

        System.out.println("db.quantity = " + savedBoard.getQuantity());
        System.out.println("db.account = " + savedBuyer.getAccount());
        System.out.println("ticket.count = " + ticketRepository.count());
        System.out.println("queue.size = " + queueSize());
        System.out.println("processing.size = " + processingSize());




        assertEquals(0, savedBoard.getQuantity());
        assertEquals(990_000, savedBuyer.getAccount());
        assertEquals(10, ticketRepository.count());
        assertEquals(0L, queueSize());


    }

    private long processingSize() {
        Long size = redisTemplate.opsForList().size("ticket:sale:processing");
        return size == null ? 0L : size;
    }

    private Member saveMember(String memberId, long account) {
        return memberRepository.save(Member.builder()
                .memberId(memberId)
                .email(memberId + "@test.com")
                .password("password")
                .nickname(memberId)
                .account(account)
                .build());
    }

    private GATicketBoard saveBoard(Member member, long quantity, long price) {
        return ticketBoardRepository.save(GATicketBoard.builder()
                .title("concert")
                .content("open run")
                .quantity(quantity)
                .price(price)
                .deadlineDateTime(LocalDateTime.now().plusDays(1))
                .member(member)
                .build());
    }

    private String redisQuantity(long boardPk) {
        return redisTemplate.opsForValue().get("ticket:inventory:ga:" + boardPk + ":quantity");
    }

    private String redisAccount(long memberPk) {
        return redisTemplate.opsForValue().get("ticket:member:" + memberPk + ":account");
    }

    private long queueSize() {
        Long size = redisTemplate.opsForList().size("ticket:sale:queue");
        return size == null ? 0L : size;
    }
    private void initializeRedisSnapshot(GATicketBoard board, Member buyer) {
        redisTemplate.opsForValue().set(
                "ticket:inventory:ga:" + board.getBoardPk() + ":quantity",
                String.valueOf(board.getQuantity())
        );
        redisTemplate.opsForValue().set(
                "ticket:member:" + buyer.getMemberPk() + ":account",
                String.valueOf(buyer.getAccount())
        );
    }
}
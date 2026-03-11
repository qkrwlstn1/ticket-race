# ticket/sale k6 부하 테스트

`@RequestMapping("ticket/sale")` + `POST /ticket/sale` 엔드포인트용 k6 시나리오입니다.

## 파일 구성

- `smoke.js`: 빠른 정상 동작 확인
- `average-load.js`: 평균 부하(일반 트래픽 가정)
- `stress.js`: 한계점 탐색
- `spike.js`: 급격한 트래픽 증가 대응 확인
- `soak.js`: 장시간 안정성 확인
- `lib/config.js`: 공통 환경 변수 및 헤더 설정
- `lib/ticketSaleScenario.js`: 공통 요청/검증 로직

## 공통 환경 변수

- `BASE_URL` (기본값: `http://localhost:8080`)
- `TICKET_SALE_PATH` (기본값: `/ticket/sale`)
- `AUTH_TOKEN` (Bearer 토큰, 인증 필요한 경우 필수)
- `BOARD_PK` (기본값: `1`)
- `AMOUNT` (기본값: `1`)
- `SLEEP_SECONDS` (기본값: `0.2`)

## 실행 예시

```bash
k6 run ticketrace/load-test/ticket-sale/smoke.js \
  -e BASE_URL=http://localhost:8080 \
  -e AUTH_TOKEN=<JWT> \
  -e BOARD_PK=1 \
  -e AMOUNT=1
```

```bash
k6 run ticketrace/load-test/ticket-sale/average-load.js -e AUTH_TOKEN=<JWT>
k6 run ticketrace/load-test/ticket-sale/stress.js -e AUTH_TOKEN=<JWT>
k6 run ticketrace/load-test/ticket-sale/spike.js -e AUTH_TOKEN=<JWT>
k6 run 
```
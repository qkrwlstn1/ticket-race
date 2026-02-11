import http from "k6/http";
import { check, sleep } from "k6";
import exec from "k6/execution";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

// 테스트를 다시 돌려도 중복 충돌을 줄이기 위한 runId(이번 실행 고유값)
const runId = Date.now();

export const options = {
    stages:[
        {duration : '30s', target: 50},
        {duration : '30s', target: 100},
        {duration : '30s', target: 150},
        {duration : '30s', target: 200}
    ],
//  vus: 100,
//  iterations: 1000, // 총 100번 실행(공유 반복 실행의 단축 옵션) :contentReference[oaicite:1]{index=1}
};

export default function () {
  // 시나리오 내에서 유니크한 iteration 번호(0부터 증가) :contentReference[oaicite:2]{index=2}
  const i = exec.scenario.iterationInTest;

  const payload = {
    id: `k6_${runId}_${i}`,
    email: `k6_${runId}_${i}@example.com`,
    password: "string",
    nickname: `k6nick_${runId}_${i}`,
  };

  const params = {
    headers: {
      "Content-Type": "application/json",
    },
  };

  // JSON.stringify로 보내야 application/json으로 제대로 전송됨 :contentReference[oaicite:3]{index=3}
  const res = http.post(`${BASE_URL}/member/signup`, JSON.stringify(payload), params);

  // 성공 코드가 200/201 중 뭐가 나오는지에 따라 조정
  check(res, {
    "status is 200 or 201": (r) => r.status === 200 || r.status === 201,
  });

  // 너무 빠르게 때리지 않으려면(선택): 0.1초 쉬기
  // sleep(0.1);
}
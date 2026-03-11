import ticketSaleScenario from "./lib/scenario.js";
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";
const runStart = new Date();
function ts(d = new Date()) {
  // Windows 파일명에서 ':'가 문제라 '-'로 치환
  // 예: 2026-02-20_13-05-09
  const pad = (n) => String(n).padStart(2, "0");
  return (
    `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` +
    `_${pad(d.getHours())}-${pad(d.getMinutes())}-${pad(d.getSeconds())}`
  );
}
export const options = {
    vus : 5,
    iterations : 5,
    thresholds: {
        checks: ["rate > 0.95"],
        http_req_failed: ["rate<0.05"],
        http_req_duration: ["p(95)<1000"],
    },
};
export default ticketSaleScenario;

export function handleSummary(data) {
  const prefix = __ENV.REPORT_PREFIX || "smoke";
  const name = `${prefix}_${ts(runStart)}.html`; // 시작시간 기준 (원하면 ts()로 종료시간 기준도 가능)

  return {
    // reports 폴더는 미리 만들어두기
    [`reports/smoke/${name}`]: htmlReport(data),
    // 콘솔에도 요약 계속 보고 싶으면 stdout 추가
    stdout: textSummary(data, { indent: " ", enableColors: true }),
  };
}
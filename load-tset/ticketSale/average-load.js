import ticketSaleScenario from "./lib/scenario.js";
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";
const runStart = new Date();
function ts(d = new Date()) {
  const pad = (n) => String(n).padStart(2, "0");
  return (
    `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` +
    `_${pad(d.getHours())}-${pad(d.getMinutes())}-${pad(d.getSeconds())}`
  );
}
const runId = Date.now();
const fileName = `avg_load_${runId}`
export const options = {
    stages : [
        { duration: "1m", target : 60},
        { duration: "30m", target : 60},
        {duration : "1m", target : 0},
    ],
    thresholds:{
        checks: ["rate>0.95"],
        http_req_failed: ["rate<0.03"],
        http_req_duration: ["p(95) < 1200"],
    },
    summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(99)"],
};

export default ticketSaleScenario;

export function handleSummary(data) {
  const prefix = __ENV.REPORT_PREFIX || "avg-load";
  const name = `${prefix}_${ts(runStart)}.html`;

  return {
    [`reports/avg-load/${name}`]: htmlReport(data),
    stdout: textSummary(data, { indent: " ", enableColors: true }),
  };
}
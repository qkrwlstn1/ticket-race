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


export const options = {
  stages: [
//    { duration: "1m", target: 100 },
    { duration: "2m", target: 10 },
    { duration: "2m", target: 20 },
    { duration: "2m", target: 30 },
    { duration: "2m", target: 40 },
    { duration: "2m", target: 50 },
    { duration: "2m", target: 60 },
    { duration: "2m", target: 70 },
    { duration: "2m", target: 80 },
    { duration: "2m", target: 90 },
//    { duration: "30s", target: 8000 },
//    { duration: "30s", target: 9000 },
//    { duration: "30s", target: 10000 },
//    { duration: "30s", target: 11000 },
//    { duration: "30s", target: 12000 },
//    { duration: "30s", target: 13000 },
//    { duration: "30s", target: 14000 },
//    { duration: "30s", target: 15000 },
//    { duration: "30s", target: 16000 },
//    { duration: "30s", target: 17000 },
//    { duration: "30s", target: 18000 },
//    { duration: "30s", target: 19000 },
//    { duration: "30s", target: 20000 },


//    { duration: "1m", target: 0 },
  ],
  thresholds: {
    http_req_failed: [{threshold: "rate<0.01", abortOnFail: true}],
    http_req_duration: ["p(95)<3000"],
  },
};

export default ticketSaleScenario;
export function handleSummary(data) {
  const prefix = __ENV.REPORT_PREFIX || "breakpoint";
  const name = `${prefix}_${ts(runStart)}.html`;

  return {
    [`reports/breakpoint/${name}`]: htmlReport(data),
    stdout: textSummary(data, { indent: " ", enableColors: true }),
  };
}
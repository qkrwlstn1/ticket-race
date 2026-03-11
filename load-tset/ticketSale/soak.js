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
  scenarios: {
    soak_0_30m: {
      executor: "constant-vus",
      exec: "ticketSale",
      vus: 60,
      duration: "30m",
      startTime: "0s",
      tags: { test_type: "soak", time_stage: "0_30m" },
    },
    soak_30_60m: {
      executor: "constant-vus",
      exec: "ticketSale",
      vus: 60,
      duration: "30m",
      startTime: "30m",
      tags: { test_type: "soak", time_stage: "30_60m" },
    },
    soak_60_90m: {
      executor: "constant-vus",
      exec: "ticketSale",
      vus: 60,
      duration: "30m",
      startTime: "60m",
      tags: { test_type: "soak", time_stage: "60_90m" },
    },
    soak_90_120m: {
      executor: "constant-vus",
      exec: "ticketSale",
      vus: 60,
      duration: "30m",
      startTime: "90m",
      tags: { test_type: "soak", time_stage: "90_120m" },
    },
  },

  thresholds: {
    checks: ["rate>0.95"],
    http_req_failed: ["rate<0.03"],

    "http_req_duration{scenario:soak_0_30m}": ["p(95)<1000"],
    "http_req_duration{scenario:soak_30_60m}": ["p(95)<1000"],
    "http_req_duration{scenario:soak_60_90m}": ["p(95)<1200"],
    "http_req_duration{scenario:soak_90_120m}": ["p(95)<1200"],

    "http_req_failed{scenario:soak_0_30m}": ["rate<0.03"],
    "http_req_failed{scenario:soak_30_60m}": ["rate<0.03"],
    "http_req_failed{scenario:soak_60_90m}": ["rate<0.05"],
    "http_req_failed{scenario:soak_90_120m}": ["rate<0.05"],
  },

  summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(99)"],
};

export function ticketSale() {
  ticketSaleScenario();
}

export function handleSummary(data) {
  const prefix = __ENV.REPORT_PREFIX || "soak";
  const name = `${prefix}_${ts(runStart)}.html`;

  return {
    [`reports/soak/${name}`]: htmlReport(data),
    stdout: textSummary(data, { indent: " ", enableColors: true }),
  };
}
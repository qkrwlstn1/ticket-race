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
    stress_80: {
      executor: "constant-vus",
      exec: "ticketSale",
      vus: 80,
      duration: "5m",
      startTime: "0s",
      tags: { test_type: "stress", load_stage: "80_vus" },
    },
    stress_100: {
      executor: "constant-vus",
      exec: "ticketSale",
      vus: 100,
      duration: "5m",
      startTime: "5m",
      tags: { test_type: "stress", load_stage: "100_vus" },
    },
    stress_120: {
      executor: "constant-vus",
      exec: "ticketSale",
      vus: 120,
      duration: "5m",
      startTime: "10m",
      tags: { test_type: "stress", load_stage: "120_vus" },
    },
    stress_140: {
      executor: "constant-vus",
      exec: "ticketSale",
      vus: 140,
      duration: "5m",
      startTime: "15m",
      tags: { test_type: "stress", load_stage: "140_vus" },
    },
    stress_160: {
      executor: "constant-vus",
      exec: "ticketSale",
      vus: 160,
      duration: "5m",
      startTime: "20m",
      tags: { test_type: "stress", load_stage: "160_vus" },
    },
  },

  thresholds: {
    checks: ["rate>0.90"],
    http_req_failed: ["rate<0.10"],

    "http_req_duration{scenario:stress_80}": ["p(95)<1000"],
    "http_req_duration{scenario:stress_100}": ["p(95)<1200"],
    "http_req_duration{scenario:stress_120}": ["p(95)<1500"],
    "http_req_duration{scenario:stress_140}": ["p(95)<2000"],
    "http_req_duration{scenario:stress_160}": ["p(95)<3000"],

    "http_req_failed{scenario:stress_80}": ["rate<0.03"],
    "http_req_failed{scenario:stress_100}": ["rate<0.05"],
    "http_req_failed{scenario:stress_120}": ["rate<0.08"],
    "http_req_failed{scenario:stress_140}": ["rate<0.10"],
    "http_req_failed{scenario:stress_160}": ["rate<0.15"],
  },

  summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(99)"],
};

export function ticketSale() {
  ticketSaleScenario();
}

export function handleSummary(data) {
  const prefix = __ENV.REPORT_PREFIX || "stress";
  const name = `${prefix}_${ts(runStart)}.html`;

  return {
    [`reports/stress/${name}`]: htmlReport(data),
    stdout: textSummary(data, { indent: " ", enableColors: true }),
  };
}
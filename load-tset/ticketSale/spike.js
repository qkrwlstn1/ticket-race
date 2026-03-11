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
    spike_120: {
      executor: "ramping-vus",
      exec: "ticketSale",
      startTime: "0s",
      stages: [
        { duration: "1m", target: 20 },
        { duration: "10s", target: 120 },
        { duration: "2m", target: 120 },
        { duration: "30s", target: 20 },
        { duration: "1m", target: 20 },
      ],
      gracefulRampDown: "10s",
      tags: { test_type: "spike", spike_stage: "120_vus" },
    },

    spike_180: {
      executor: "ramping-vus",
      exec: "ticketSale",
      startTime: "4m40s",
      stages: [
        { duration: "1m", target: 20 },
        { duration: "10s", target: 180 },
        { duration: "2m", target: 180 },
        { duration: "30s", target: 20 },
        { duration: "1m", target: 20 },
      ],
      gracefulRampDown: "10s",
      tags: { test_type: "spike", spike_stage: "180_vus" },
    },

    spike_240: {
      executor: "ramping-vus",
      exec: "ticketSale",
      startTime: "9m20s",
      stages: [
        { duration: "1m", target: 20 },
        { duration: "10s", target: 240 },
        { duration: "2m", target: 240 },
        { duration: "30s", target: 20 },
        { duration: "1m", target: 20 },
      ],
      gracefulRampDown: "10s",
      tags: { test_type: "spike", spike_stage: "240_vus" },
    },
  },

  thresholds: {
    checks: ["rate>0.90"],
    http_req_failed: ["rate<0.10"],

    "http_req_duration{scenario:spike_120}": ["p(95)<1500"],
    "http_req_duration{scenario:spike_180}": ["p(95)<2000"],
    "http_req_duration{scenario:spike_240}": ["p(95)<3000"],

    "http_req_failed{scenario:spike_120}": ["rate<0.05"],
    "http_req_failed{scenario:spike_180}": ["rate<0.10"],
    "http_req_failed{scenario:spike_240}": ["rate<0.15"],
  },

  summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(99)"],
};

export function ticketSale() {
  ticketSaleScenario();
}

export function handleSummary(data) {
  const prefix = __ENV.REPORT_PREFIX || "spike";
  const name = `${prefix}_${ts(runStart)}.html`;

  return {
    [`reports/spike/${name}`]: htmlReport(data),
    stdout: textSummary(data, { indent: " ", enableColors: true }),
  };
}
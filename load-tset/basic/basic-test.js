import http from "k6/http"
import { check, sleep } from "k6";
import { Counter } from "k6/metrics";
export const BASE_URL = __ENV.BASE_URL || "http://localhost:8080"
export const TICKET_BOARD_PATH = __ENV.TICKET_BOARD_PATH || "/ticket/board/article/1"

const status200 = new Counter("http_status_200");
const statusOther = new Counter("http_status_other");



export const options = {
    stages : [
        { duration: "30s", target : 50},
        { duration: "10m", target : 600},
        { duration: "1m", target : 0},

    ],
    thresholds:{
        checks: ["rate>0.95"],
        http_req_failed: ["rate<0.03"],
        http_req_duration: ["p(95) < 1200"],
    },
    summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(99)"],
};






export function buildHeaders(){
    const headers = {"Content-type" : "application/json",
        "Authorization" : "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ0aWNrZXRyYWNlIiwic3ViIjoiMSIsImlhdCI6MTc3MzMxMjY4NiwiZXhwIjoxNzk5MjMyNjg2LCJ0eXAiOiJhY2Nlc3MifQ.2T6o-N7d4ZVVnUm_IAAHCThBAjo8r1DLUB0QNY-Pc3LfF2Awvu-R7LImckUTtlKZgXvJxm-rdjGAiEgN3UBaRA",
    };

    return headers;
}

export default function ticketBoardScenario(){
    const response = http.get(
        `${BASE_URL}${TICKET_BOARD_PATH}`,
        {
            headers: buildHeaders(),
        }
    );

    check(response,{
        "status is 200": (res) => res.status === 200,
        "request duration < 10000ms" : (res) => res.timings.duration < 10000,
    });

    if (response.status === 200) status200.add(1);
    else statusOther.add(1);

    sleep(Number(__ENV.SLEEP_SECONDS || 0.2));
}
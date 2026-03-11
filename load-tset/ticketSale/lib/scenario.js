import http from "k6/http"
import { check, sleep } from "k6";
import { BASE_URL, TICKET_SALE_PATH, REQUEST_BODY, buildHeaders} from "./config.js";
import { Counter } from "k6/metrics";


const status201 = new Counter("http_status_201");
const status409 = new Counter("http_status_409");
const statusOther = new Counter("http_status_other");
http.setResponseCallback(http.expectedStatuses(201, 409));
export default function ticketSaleScenario(){
    const response = http.post(
        `${BASE_URL}${TICKET_SALE_PATH}`,
        JSON.stringify(REQUEST_BODY),
        {
            headers: buildHeaders(),
        }
    );

    check(response,{
        "status is 201 or 409": (res) => res.status === 201 || res.status === 409,
        "request duration < 10000ms" : (res) => res.timings.duration < 10000,
    });

     if (response.status === 201) status201.add(1);
        else if (response.status === 409) status409.add(1);
        else statusOther.add(1);

    sleep(Number(__ENV.SLEEP_SECONDS || 0.2));
}
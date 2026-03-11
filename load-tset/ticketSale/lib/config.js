export const BASE_URL = __ENV.BASE_URL || "http://localhost:8080"
export const TICKET_SALE_PATH = __ENV.TICKET_SALE_PATH || "/ticket/sale/test"

export const REQUEST_BODY = {
    boardPk : Number(__ENV.BOARD_PO || 1),
    amount : Number(__ENV.AMOUNT || 1),
};

export function buildHeaders(){
    const headers = {"Content-type" : "application/json",
        "Authorization" : "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ0aWNrZXRyYWNlIiwic3ViIjoiMSIsImlhdCI6MTc3MDgxNjI0NSwiZXhwIjoxNzczNDA4MjQ1LCJ0eXAiOiJhY2Nlc3MifQ.GzK95wcnFv9l_aRlqKILJ24nRFI3RtLKpKXL5Vy6XsxKKDbGTPCnz1T8im907-hluAAVL-JxOULiaOeBvOglgw",
    };

    return headers;
}
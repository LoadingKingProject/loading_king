// --- [API Configuration] ---
const API_BASE = '/api';
const API_AREA = '/api-v1/area';

// 전역 토큰 관리
let jwtToken = localStorage.getItem('accessToken');

// 서버에서 토큰을 주입받은 경우 (Thymeleaf 변수 처리)
if (typeof SERVER_TOKEN !== 'undefined' && SERVER_TOKEN) {
    jwtToken = SERVER_TOKEN;
}

function getHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    if (jwtToken) {
        headers['Authorization'] = 'Bearer ' + jwtToken;
    }
    return headers;
}
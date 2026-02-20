// --- [1. Login] ---
async function login() {
    // 1. HTML의 ID를 기준으로 정확한 입력창 찾기
    const idInput = document.getElementById('loginId');
    const pwInput = document.getElementById('loginPw');

    // 2. 입력값이 없으면 개발용 기본값(DRIVER_01) 사용
    const id = idInput ? idInput.value : "DRIVER_01";
    const pw = pwInput ? pwInput.value : "1234";

    try {
        const response = await fetch(`${API_BASE}/auth/dev-login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId: id, password: pw })
        });

        if (response.ok) {
            const data = await response.json();

            // 토큰 저장
            if (data.token) {
                localStorage.setItem('accessToken', data.token);
            }

            // 페이지 이동
            window.location.href = '/drive';
        } else {
            const errorText = await response.text();
            alert("로그인 실패: " + errorText);
        }
    } catch (e) {
        console.error(e);
        alert("서버 연결 실패");
    }
}
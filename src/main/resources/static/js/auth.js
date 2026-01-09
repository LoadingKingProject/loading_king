// --- [1. Login] ---
async function login() {
    // HTML 분리 시 input ID가 변경될 수 있으므로 확인 필요
    // 예: <input id="loginId">, <input id="loginPw">
    const idInput = document.querySelector('input[type="text"]'); // 또는 document.getElementById('loginId')
    const pwInput = document.querySelector('input[type="password"]'); // 또는 document.getElementById('loginPw')

    const id = idInput ? idInput.value : "DRIVER_01";
    const pw = pwInput ? pwInput.value : "1234";

    try {
        const response = await fetch(`${API_BASE}/auth/dev-login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId: id, password: pw }) // API 스펙에 맞게 필드명 조정 필요할 수 있음 (현재 코드는 fetch 옵션만 있고 body가 빠져있어 보강함)
        });

        if (response.ok) {
            const data = await response.json();
            jwtToken = data.token;
            localStorage.setItem('accessToken', jwtToken);

            alert(data.message || "로그인 성공");

            // ★ 수정됨: SPA 방식의 goToPhase 대신 페이지 이동
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
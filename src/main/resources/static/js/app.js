let sectors = [];
let scannedCounts = {};


// --- [Navigation Logic] ---
function goToPhase(phaseId) {
    if (!jwtToken) {
        alert("로그인이 필요합니다.");
        window.location.href = '/login'; // 로그인 페이지로 튕겨내기
        return;
    }

    // 섹터 검증 (섹터 설정 단계에서 넘어갈 때)
    if (phaseId === 'setup4' || phaseId === 'op_scan') { // setup3 -> setup4 로직 보완
        // 현재 sectors가 비어있다면 서버에서 다시 가져와보기
        if (sectors.length === 0) fetchSectors().then(() => {
            if (sectors.length === 0) alert("섹터를 먼저 생성해주세요.");
        });
    }

    // 순서 설정 단계 진입 시 리스트 렌더링
    if (phaseId === 'setup4') {
        renderOrderList();
    }

    // 모든 phase 숨기고 타겟만 보이기
    document.querySelectorAll('.phase').forEach(el => {
        el.style.display = 'none'; // 혹은 el.classList.remove('active');
        if(el.classList.contains('active')) el.classList.remove('active');
    });

    const areaOverlay = document.getElementById('areaConfirmOverlay');
    if(areaOverlay) areaOverlay.style.display = 'none';

    const target = document.getElementById(phaseId);
    if(target) {
        target.style.display = 'block'; // 혹은 target.classList.add('active');
        target.classList.add('active');
    }

    // 단계별 추가 로직 실행
    if (phaseId === 'op_load') loadRouteOrder(true);
    if (phaseId === 'op_deliver') loadRouteOrder(false);
}


// --- [Sector Logic] ---
function updateRadius(val) {
    const circle = document.getElementById('radiusCircle');
    if(circle) {
        document.getElementById('radiusVal').innerText = val + 'm';
        circle.style.width = (val * 2) + 'px';
        circle.style.height = (val * 2) + 'px';
    }
}

async function addSector() {
    const nameInput = document.getElementById('sectorNameInput');
    const name = nameInput.value.trim();
    const radiusText = document.getElementById('radiusVal').innerText;
    const radius = parseFloat(radiusText);

    // 시뮬레이션용 랜덤 좌표 (실제로는 지도 중심좌표 등을 사용해야 함)
    const lat = 37.76 + (Math.random() * 0.01);
    const lng = 126.70 + (Math.random() * 0.01);

    if (!name) return alert("섹터 이름을 입력해주세요");

    try {
        const response = await fetch(`${API_BASE}/sectors`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({ name, lat, lng, radius })
        });

        if (response.ok) {
            alert(`[${name}] 섹터 생성 완료!`);
            nameInput.value = '';
            fetchSectors();
        } else {
            alert("섹터 생성 실패");
        }
    } catch (e) { console.error(e); }
}

async function fetchSectors() {
    try {
        const response = await fetch(`${API_BASE}/sectors`, {
            method: 'GET',
            headers: getHeaders()
        });
        if (response.ok) {
            sectors = await response.json();
            const badge = document.getElementById('sectorCountBadge');
            if(badge) badge.innerText = `현재 ${sectors.length}개 생성됨`;
            renderSectorList();
        }
    } catch (e) { console.error(e); }
}

function renderSectorList() {
    const container = document.getElementById('createdSectorList');
    if(!container) return;

    container.innerHTML = sectors.map(sec => `
        <div class="list-item">
            <div style="display:flex; align-items:center;">
                <div style="background:var(--accent-color); width:30px; height:30px; border-radius:50%; text-align:center; line-height:30px; font-weight:bold; color:black; margin-right:10px;">${sec.id}</div>
                <div>
                    <div style="font-weight:bold;">${sec.sectorName}</div>
                    <div style="font-size:0.8rem; color:var(--text-sub);">ID: ${sec.id}</div>
                </div>
            </div>
            <i class="fa-solid fa-check" style="color:var(--success-color);"></i>
        </div>
    `).join('');
}


// --- [Order Logic] ---
function renderOrderList() {
    const container = document.getElementById('orderListContainer');
    if(!container) return;

    if(sectors.length === 0) fetchSectors(); // 데이터 없으면 로드 시도

    container.innerHTML = sectors.map((sec, idx) => `
        <div class="list-item">
            <div style="display:flex; align-items:center;">
                <span style="color:var(--primary-color); font-weight:bold; margin-right:15px;">${idx + 1}</span>
                <div><div style="font-weight:bold;">${sec.sectorName}</div></div>
            </div>
            <div class="order-controls">
                <button onclick="moveUp(${idx})">▲</button>
                <button onclick="moveDown(${idx})">▼</button>
            </div>
        </div>
    `).join('');
}

function moveUp(idx) {
    if (idx === 0) return;
    [sectors[idx], sectors[idx - 1]] = [sectors[idx - 1], sectors[idx]];
    renderOrderList();
}
function moveDown(idx) {
    if (idx === sectors.length - 1) return;
    [sectors[idx], sectors[idx + 1]] = [sectors[idx + 1], sectors[idx]];
    renderOrderList();
}

async function finalizeSetup() {
    if (!confirm("이 순서대로 배송하시겠습니까?")) return;
    try {
        const response = await fetch(`${API_BASE}/routes/order`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(sectors.map(s => s.id))
        });
        if (response.ok) goToPhase('op_scan');
        else alert("순서 저장 실패");
    } catch (e) { alert("통신 오류"); }
}


// --- [Operation Logic] ---
async function simulateScanAction() {
    const mockBarcode = "ITEM-" + Math.floor(Math.random() * 10000);
    try {
        const response = await fetch(`${API_BASE}/logistics/scan`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({ barcode: mockBarcode, address: "경기 파주시..." })
        });

        if (response.ok) {
            const data = await response.json();
            const resCard = document.getElementById('scanResultCard');
            if(resCard) resCard.style.display = 'block';

            document.getElementById('scanResultTitle').innerText = data.assigned ? "배정" : "미배정";
            document.getElementById('scanResultDesc').innerText = data.assigned ? data.sectorName : "구역 없음";

            if(data.assigned) {
                const totalElem = document.getElementById('scanTotalCount');
                if(totalElem) totalElem.innerText = (parseInt(totalElem.innerText) || 0) + 1 + "개";
            }
        }
    } catch (e) { alert("통신 오류"); }
}

async function loadRouteOrder(isLIFO) {
    try {
        const response = await fetch(`${API_BASE}/routes/order`, { method: 'GET', headers: getHeaders() });
        if (response.ok) {
            const orderIds = await response.json();
            if(sectors.length === 0) await fetchSectors();

            let orderedSectors = orderIds.map(id => sectors.find(s => s.id === id)).filter(s => s);

            if (isLIFO) renderLoadingList(orderedSectors.reverse());
            else renderDeliveryList(orderedSectors);
        }
    } catch (e) {}
}

function renderLoadingList(list) {
    const el = document.getElementById('loadingGuideList');
    if(el) {
        el.innerHTML = list.map((sec, idx) => `
        <div class="card" style="border-left: 4px solid var(--danger-color);">
            <h3>${sec.sectorName}</h3>
            <p>적재순서 ${idx + 1} (안쪽 배치)</p>
        </div>`).join('');
    }
}

function renderDeliveryList(list) {
    const el = document.getElementById('deliveryRouteList');
    if(el) {
        el.innerHTML = list.map((sec, idx) => `
        <div class="card" style="border-left: 4px solid var(--success-color);">
            <h3>${sec.sectorName}</h3>
            <p>STOP ${idx + 1} (배송 예정)</p>
        </div>`).join('');
    }
}


// --- [Initialization] ---
// drive.html이 로드되면 자동으로 실행
document.addEventListener("DOMContentLoaded", function() {
    // 1. 지도 초기화
    if(typeof initMap === 'function') initMap('map');

    // 2. 시/도 목록 로드
    if(typeof loadCities === 'function') loadCities();

    // 3. 첫 화면(구역 선택) 보이기
    goToPhase('phase_area');
});
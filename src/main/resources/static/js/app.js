let sectors = [];
let scannedCounts = {};
window.scanDeliveryOrder = window.scanDeliveryOrder || [];


// --- [Navigation Logic] ---
function goToPhase(phaseId) {
    if (!jwtToken) {
        alert("로그인이 필요합니다.");
        window.location.href = '/login'; // 로그인 페이지로 튕겨내기
        return;
    }

    // 섹터 검증 (섹터 설정 단계에서 넘어갈 때)
    if (phaseId === 'setup4' || phaseId === 'op_scan') {
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
    document.querySelectorAll('.phase').forEach((el) => {
        el.classList.remove('active');
        el.style.display = 'none';
    });

    const areaOverlay = document.getElementById('areaConfirmOverlay');
    if(areaOverlay) areaOverlay.style.display = 'none';

    // 지도가 필요한 phase에서만 보이게, 나머지에서는 숨기기
    const mapEl = document.getElementById('map');
    const mapPhases = ['phase_area', 'phase_sector', 'setup4', 'op_scan_order'];
    if (mapEl) {
        mapEl.style.display = mapPhases.includes(phaseId) ? 'block' : 'none';
    }

    const target = document.getElementById(phaseId);
    if (!target) {
        console.error(`[goToPhase] target phase not found: ${phaseId}`);
        alert(`화면 전환 실패: ${phaseId}`);
        return;
    }
    target.classList.add('active');
    target.style.display = 'flex';

    // 단계별 추가 로직 실행
    if (phaseId === 'op_scan_order') {
        const cameraOverlay = document.getElementById('cameraOverlay');
        const showOverlayBtn = document.getElementById('btnShowOverlay');
        if (cameraOverlay) cameraOverlay.style.display = 'none';
        if (showOverlayBtn) showOverlayBtn.style.display = 'none';
        // 항상 서버에서 섹터 데이터를 강제로 로드하여 빈 화면 방지
        if (typeof window.forceFillAndRenderOrder === 'function') {
            window.forceFillAndRenderOrder();
        } else if (typeof window.renderScanOrderEditor === 'function') {
            window.renderScanOrderEditor();
        }
        // 지도에 섹터 표시
        if (typeof window.renderSectorsOnMap === 'function' && sectors.length > 0) {
            window.renderSectorsOnMap(sectors);
        } else if (sectors.length === 0) {
            fetchSectors().then(() => {
                if (typeof window.renderSectorsOnMap === 'function') {
                    window.renderSectorsOnMap(sectors);
                }
            });
        }
    }
    if (phaseId === 'op_load') loadRouteOrder(true);
    if (phaseId === 'op_deliver') loadRouteOrder(false);
    if (phaseId === 'op_scan' && typeof window.startScanCamera === 'function') {
        window.startScanCamera();
        if (typeof window.loadSavedItemsForScan === 'function') {
            window.loadSavedItemsForScan();
        }
        if (typeof window.initSectorDashboard === 'function') {
            window.initSectorDashboard();
        }
    }
    if (phaseId === 'phase_sector' && typeof enableSectorPlacement === 'function') {
        enableSectorPlacement();
        fetchSectors();
    }
    if (phaseId !== 'phase_sector' && typeof disableSectorPlacement === 'function') {
        disableSectorPlacement();
    }
    if (phaseId !== 'op_scan' && typeof window.stopScanCamera === 'function') {
        window.stopScanCamera();
    }
}


// --- [Sector Logic] ---
function updateRadius(val) {
    const radiusVal = document.getElementById('radiusVal');
    if (radiusVal) radiusVal.innerText = `${val}m`;
    if (typeof window.updateSectorPreview === 'function') {
        window.updateSectorPreview(Number(val));
    }
}

async function createSectorFromPin() {
    if (!window.selectedVillageId) return alert("먼저 동을 선택해주세요.");
    if (!window.pendingSectorCenter) return alert("지도에서 섹터 중심점을 선택해주세요.");

    const nameInput = document.getElementById('sectorNameInput');
    const name = nameInput?.value?.trim() || "Sector";
    const radiusText = document.getElementById('radiusVal')?.innerText || "100m";
    const radius = parseFloat(radiusText);
    if (!Number.isFinite(radius) || radius <= 0) return alert("반경을 확인해주세요.");

    const { lat, lng } = window.pendingSectorCenter;
    const isEditing = !!window.editingSectorId;
    const endpoint = isEditing ? `${API_BASE}/sectors/${window.editingSectorId}` : `${API_BASE}/sectors`;
    const method = isEditing ? 'PUT' : 'POST';
    const payload = isEditing
        ? { name, radius, villageId: window.selectedVillageId }
        : { name, lat, lng, radius, villageId: window.selectedVillageId };

    try {
        const response = await fetch(endpoint, {
            method,
            headers: getHeaders(),
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            const created = await response.json();
            sectors = Array.isArray(sectors) ? sectors : [];
            if (isEditing) {
                const idx = sectors.findIndex((s) => s.id === window.editingSectorId);
                if (idx !== -1) sectors[idx] = created;
            } else {
                sectors.push(created);
            }
            nameInput.value = '';
            resetSectorEditState();
            renderSectorList();
            if (typeof window.renderSectorsOnMap === 'function') {
                window.renderSectorsOnMap(sectors);
            }
            await fetchSectors();
        } else {
            alert("섹터 생성 실패");
        }
    } catch (e) {
        console.error(e);
        alert("섹터 생성 실패");
    }
}

function selectSectorForEdit(sectorId) {
    const sector = sectors.find((s) => s.id === sectorId);
    if (!sector) return;
    if (typeof window.panToSector === 'function') {
        window.panToSector(sector.lat, sector.lng);
    }
    window.editingSectorId = sectorId;

    const nameInput = document.getElementById('sectorNameInput');
    if (nameInput) nameInput.value = sector.sectorName ?? '';

    const radius = estimateRadiusMeters(sector);
    updateRadius(radius);

    if (sector.lat != null && sector.lng != null) {
        window.pendingSectorCenter = { lat: sector.lat, lng: sector.lng };
        if (typeof window.drawSectorPreview === 'function') {
            window.drawSectorPreview();
        } else if (typeof window.updateSectorPreview === 'function') {
            window.updateSectorPreview(radius);
        }
    }

    const btn = document.getElementById('sectorActionBtn');
    if (btn) btn.textContent = "수정 완료";
}

function resetSectorEditState() {
    window.editingSectorId = null;
    const btn = document.getElementById('sectorActionBtn');
    if (btn) btn.textContent = "완료";
}

function estimateRadiusMeters(sector) {
    if (!sector || !Array.isArray(sector.boundary) || sector.boundary.length === 0) {
        return 100;
    }
    const centerLat = sector.lat;
    const centerLng = sector.lng;
    if (centerLat == null || centerLng == null) return 100;
    let max = 0;
    sector.boundary.forEach((p) => {
        const d = haversineMeters(centerLat, centerLng, p.lat, p.lng);
        if (d > max) max = d;
    });
    return Math.max(50, Math.round(max));
}

function haversineMeters(lat1, lng1, lat2, lng2) {
    const R = 6371000;
    const toRad = (v) => (v * Math.PI) / 180;
    const dLat = toRad(lat2 - lat1);
    const dLng = toRad(lng2 - lng1);
    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
        Math.sin(dLng / 2) * Math.sin(dLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

async function fetchSectors() {
    try {
        const response = await fetch(`${API_BASE}/sectors`, {
            method: 'GET',
            headers: getHeaders()
        });
        if (response.ok) {
            sectors = await response.json();
            if (window.editingSectorId && !sectors.some((s) => s.id === window.editingSectorId)) {
                resetSectorEditState();
            }
            const badge = document.getElementById('sectorCountBadge');
            if(badge) badge.innerText = `현재 ${sectors.length}개 생성됨`;
            renderSectorList();
            if (typeof window.renderSectorsOnMap === 'function') {
                window.renderSectorsOnMap(sectors);
            }
        }
    } catch (e) { console.error(e); }
}

function renderSectorList() {
    const container = document.getElementById('createdSectorList');
    if(!container) return;

    if (sectors.length === 0) {
        container.innerHTML = '<p style="text-align:center; color:#999; font-size:0.9rem;">섹터가 아직 없습니다.</p>';
        return;
    }

    container.innerHTML = sectors.map(sec => `
        <div class="list-item sector-item" onclick="selectSectorForEdit(${sec.id})" style="cursor:pointer;">
            <div style="display:flex; align-items:center;">
                <div style="background:var(--accent-color); min-width:54px; height:30px; border-radius:16px; text-align:center; line-height:30px; font-weight:bold; color:black; margin-right:10px; padding:0 10px;">${sec.villageName ?? '동'}</div>
                <div>
                    <div style="font-weight:bold;">${sec.sectorName}</div>
                    <div style="font-size:0.8rem; color:var(--text-sub);">ID: ${sec.id}</div>
                </div>
            </div>
            <button class="btn btn-dark" style="width:auto; padding:6px 10px; margin:0;" onclick="deleteSector(${sec.id}, event)">
                삭제
            </button>
        </div>
    `).join('');
}

async function deleteSector(sectorId, event) {
    if (event) event.stopPropagation();
    if (!confirm("이 섹터를 삭제할까요?")) return;

    try {
        const response = await fetch(`${API_BASE}/sectors/${sectorId}`, {
            method: 'DELETE',
            headers: getHeaders()
        });
        if (!response.ok) {
            alert("섹터 삭제 실패");
            return;
        }
        if (window.editingSectorId === sectorId) {
            resetSectorEditState();
        }
        await fetchSectors();
    } catch (e) {
        console.error(e);
        alert("섹터 삭제 실패");
    }
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
    const mockBarcode =
        "|V1|10305871128210|송파구 백제고분로 40길 24|정봄빌딩||3층|정*준|499b8997|37.502565008848094|127.10704897921262|B36|1764280800000|";
    try {
        const response = await fetch(`${API_BASE}/logistics/scan`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({ barcode: mockBarcode })
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
    if (Array.isArray(window.scanDeliveryOrder) && window.scanDeliveryOrder.length > 0) {
        const ordered = window.scanDeliveryOrder.map(item => ({
            sectorId: item.sectorId ?? null,
            sectorName: item.sectorName ?? '미지정',
            count: item.count ?? 0
        }));
        if (isLIFO) renderLoadingList([...ordered].reverse());
        else renderDeliveryList(ordered);
        return;
    }

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
            <p style="margin:4px 0 0; color:var(--text-sub); font-size:0.85rem;">스캔 건수: ${sec.count ?? 0}</p>
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
            <p style="margin:4px 0 0; color:var(--text-sub); font-size:0.85rem;">스캔 건수: ${sec.count ?? 0}</p>
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
window.goToPhase = goToPhase;
console.log("[app.js loaded] goToPhase 등록됨:", typeof window.goToPhase);

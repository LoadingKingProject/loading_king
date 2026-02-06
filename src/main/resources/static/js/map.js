let map; // 지도 객체
let polygons = [];
let sectorMarker = null;
let sectorCircle = null;
let sectorClickListener = null;
let sectorPolygons = [];
let sectorOverlays = [];
let scanMarker = null;
let savedItemMarkers = [];

// 1. 지도 초기화
function initMap(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const options = {
        center: new kakao.maps.LatLng(37.5665, 126.9780), // 서울 시청 중심
        level: 8
    };
    map = new kakao.maps.Map(container, options);
}

// 2. 시/도 목록 조회
async function loadCities() {
    try {
        const res = await fetch(`${API_AREA}/cities`, { headers: getHeaders() });
        if (res.ok) {
            const cities = await res.json();
            const select = document.getElementById('citySelect');
            if(!select) return;

            select.innerHTML = '<option value="">시/도 선택</option>';
            cities.forEach(city => {
                const option = document.createElement('option');
                option.value = city.id;
                option.text = city.name;
                select.appendChild(option);
            });
        }
    } catch (e) { console.error("시/도 로드 실패:", e); }
}

// 3. 시/도 선택 핸들러
async function handleCityChange() {
    const cityId = document.getElementById('citySelect').value;
    const districtSelect = document.getElementById('districtSelect');
    const villageSelect = document.getElementById('villageSelect');

    districtSelect.innerHTML = '<option value="">시/군/구</option>';
    villageSelect.innerHTML = '<option value="">읍/면/동</option>';
    districtSelect.disabled = true;
    villageSelect.disabled = true;

    resetUI();

    if (!cityId) return;

    try {
        const res = await fetch(`${API_AREA}/${cityId}/districts`, { headers: getHeaders() });
        if (res.ok) {
            const districts = await res.json();
            districts.forEach(dist => {
                const option = document.createElement('option');
                option.value = dist.id;
                option.text = dist.name;
                districtSelect.appendChild(option);
            });
            districtSelect.disabled = false;
        }
    } catch (e) { console.error("시/군/구 로드 실패:", e); }
}

// 4. 시/군/구 선택 핸들러
async function handleDistrictChange() {
    const districtId = document.getElementById('districtSelect').value;
    const villageSelect = document.getElementById('villageSelect');

    villageSelect.innerHTML = '<option value="">읍/면/동</option>';
    villageSelect.disabled = true;

    resetUI();

    if (!districtId) return;

    try {
        const res = await fetch(`${API_AREA}/${districtId}/villages`, { headers: getHeaders() });
        if (res.ok) {
            const villages = await res.json();
            villages.forEach(vill => {
                const option = document.createElement('option');
                option.value = vill.id;
                option.text = vill.name;
                villageSelect.appendChild(option);
            });
            villageSelect.disabled = false;
        }
    } catch (e) { console.error("읍/면/동 로드 실패:", e); }
}

// 5. 읍/면/동 선택 핸들러 (문제 해결 로직 적용)
async function handleVillageChange() {
    const villageId = document.getElementById('villageSelect').value;
    const villageSelect = document.getElementById('villageSelect');

    const overlay = document.getElementById('areaConfirmOverlay');
    const areaNameDisplay = document.getElementById('selectedAreaName');

    resetUI();

    if (!villageId) return;

    try {
        console.log(`[Loading] Village ID: ${villageId}`);
        const res = await fetch(`${API_AREA}/villages/${villageId}/map`, { headers: getHeaders() });

        if (res.ok) {
            const data = await res.json();
            console.log("Map Data:", data);

            if (typeof kakao === 'undefined' || !kakao.maps) {
                alert("카카오맵 로드 실패");
                return;
            }

            // 필드명 보정 (coordinates 또는 points 둘 다 대응)
            const pointsData = data.coordinates || data.points;

            if (pointsData && pointsData.length > 0) {
                window.selectedVillageId = Number(villageId);
                window.selectedVillageName = villageSelect.options[villageSelect.selectedIndex].text;

                // [★ 핵심 수정] 좌표 자동 보정 로직
                const path = pointsData.map(p => {
                    // 서버에서 lat, lng 필드로 오는지, x, y로 오는지 확인하여 값 추출
                    let lat = p.lat !== undefined ? p.lat : p.y;
                    let lng = p.lng !== undefined ? p.lng : p.x;

                    // [안전장치] 위도(Lat)가 90보다 크면, 위도와 경도가 바뀐 것임. (대한민국 위도는 33~38)
                    // 이 경우 두 값을 바꿔서 넣어줌.
                    if (lat > 90) {
                        return new kakao.maps.LatLng(lng, lat);
                    }
                    return new kakao.maps.LatLng(lat, lng);
                });

                const polygon = new kakao.maps.Polygon({
                    map: map,
                    path: path,
                    strokeWeight: 2,
                    strokeColor: '#004c80',
                    strokeOpacity: 0.8,
                    strokeStyle: 'solid',
                    fillColor: '#fff',
                    fillOpacity: 0.4
                });
                polygons.push(polygon);

                // 지도 범위 재설정 (Bounds)
                const bounds = new kakao.maps.LatLngBounds();
                path.forEach(coord => bounds.extend(coord));
                map.setBounds(bounds);

                // 오버레이 표시
                if (overlay && areaNameDisplay) {
                    areaNameDisplay.innerText = window.selectedVillageName;
                    overlay.style.display = 'block';
                }
            } else {
                console.warn("좌표 데이터가 비어있습니다.");
            }
        } else {
            alert("구역 정보 로드 실패");
        }
    } catch (e) {
        console.error(e);
    }
}

function clearPolygons() {
    polygons.forEach(p => p.setMap(null));
    polygons = [];
}

// [필수 함수] resetUI 정의
function resetUI() {
    clearPolygons();
    const overlay = document.getElementById('areaConfirmOverlay');
    if (overlay) {
        overlay.style.display = 'none';
    }
    window.selectedVillageId = null;
    window.selectedVillageName = null;
    clearSectorPreview();
    clearSectorPolygons();
}

function enableSectorPlacement() {
    if (!map || sectorClickListener) return;
    sectorClickListener = kakao.maps.event.addListener(map, 'click', function(mouseEvent) {
        const latlng = mouseEvent.latLng;
        window.pendingSectorCenter = { lat: latlng.getLat(), lng: latlng.getLng() };
        drawSectorPreview();
    });
}

function disableSectorPlacement() {
    if (map && sectorClickListener) {
        kakao.maps.event.removeListener(map, 'click', sectorClickListener);
        sectorClickListener = null;
    }
}

function drawSectorPreview() {
    if (!map || !window.pendingSectorCenter) return;

    const { lat, lng } = window.pendingSectorCenter;
    const center = new kakao.maps.LatLng(lat, lng);

    if (sectorMarker) sectorMarker.setMap(null);
    sectorMarker = new kakao.maps.Marker({ position: center });
    sectorMarker.setMap(map);

    const radiusText = document.getElementById('radiusVal')?.innerText || "100m";
    const radius = parseFloat(radiusText);
    updateSectorPreview(radius);
}

function updateSectorPreview(radius) {
    if (!map || !window.pendingSectorCenter || !Number.isFinite(radius)) return;
    const { lat, lng } = window.pendingSectorCenter;
    const center = new kakao.maps.LatLng(lat, lng);

    if (sectorCircle) sectorCircle.setMap(null);
    sectorCircle = new kakao.maps.Circle({
        center,
        radius,
        strokeWeight: 2,
        strokeColor: '#ff6b6b',
        strokeOpacity: 0.8,
        strokeStyle: 'solid',
        fillColor: '#ff6b6b',
        fillOpacity: 0.2
    });
    sectorCircle.setMap(map);
}

function clearSectorPreview() {
    if (sectorMarker) {
        sectorMarker.setMap(null);
        sectorMarker = null;
    }
    if (sectorCircle) {
        sectorCircle.setMap(null);
        sectorCircle = null;
    }
    window.pendingSectorCenter = null;
}

function renderSectorsOnMap(sectors) {
    clearSectorPolygons();
    if (!Array.isArray(sectors) || sectors.length === 0) return;
    if (typeof kakao === 'undefined' || !kakao.maps) return;

    sectors.forEach((sec) => {
        if (!sec.boundary || sec.boundary.length === 0) return;
        const path = sec.boundary.map(p => new kakao.maps.LatLng(p.lat, p.lng));
        const polygon = new kakao.maps.Polygon({
            map,
            path,
            strokeWeight: 2,
            strokeColor: '#F97316',
            strokeOpacity: 0.9,
            strokeStyle: 'solid',
            fillColor: '#F97316',
            fillOpacity: 0.15
        });
        sectorPolygons.push({ id: sec.id, polygon });

        const center = computeBoundaryCenter(sec.boundary);
        if (center) {
            const overlay = new kakao.maps.CustomOverlay({
                position: new kakao.maps.LatLng(center.lat, center.lng),
                content: `<div class="sector-label">${sec.sectorName ?? 'Sector'}</div>`
            });
            overlay.setMap(map);
            sectorOverlays.push({ id: sec.id, overlay });
        }
    });
}

function clearSectorPolygons() {
    sectorPolygons.forEach(p => p.polygon.setMap(null));
    sectorPolygons = [];
    sectorOverlays.forEach(o => o.overlay.setMap(null));
    sectorOverlays = [];
}

function computeBoundaryCenter(boundary) {
    if (!Array.isArray(boundary) || boundary.length === 0) return null;
    let sumLat = 0;
    let sumLng = 0;
    boundary.forEach(p => {
        sumLat += p.lat;
        sumLng += p.lng;
    });
    return { lat: sumLat / boundary.length, lng: sumLng / boundary.length };
}

function panToSector(lat, lng) {
    if (!map || lat == null || lng == null) return;
    map.panTo(new kakao.maps.LatLng(lat, lng));
}

function highlightSectorById(sectorId) {
    if (!sectorId) return;
    sectorPolygons.forEach(({ id, polygon }) => {
        if (id === sectorId) {
            polygon.setOptions({
                strokeColor: '#10B981',
                fillColor: '#10B981',
                fillOpacity: 0.25
            });
        } else {
            polygon.setOptions({
                strokeColor: '#F97316',
                fillColor: '#F97316',
                fillOpacity: 0.15
            });
        }
    });
}

function showScanMarker(lat, lng) {
    if (!map || lat == null || lng == null) return;
    const position = new kakao.maps.LatLng(lat, lng);
    if (scanMarker) scanMarker.setMap(null);
    scanMarker = new kakao.maps.Marker({ position });
    scanMarker.setMap(map);
    map.panTo(position);
}

function clearSavedItemMarkers() {
    savedItemMarkers.forEach((marker) => marker.setMap(null));
    savedItemMarkers = [];
}

function showSavedItemMarkers(items) {
    if (!map) return;
    clearSavedItemMarkers();
    if (!Array.isArray(items)) return;

    items.forEach((item) => {
        if (item?.lat == null || item?.lng == null) return;
        const position = new kakao.maps.LatLng(item.lat, item.lng);
        const marker = new kakao.maps.Marker({ position });
        marker.setMap(map);
        savedItemMarkers.push(marker);
    });
}

window.enableSectorPlacement = enableSectorPlacement;
window.disableSectorPlacement = disableSectorPlacement;
window.updateSectorPreview = updateSectorPreview;
window.renderSectorsOnMap = renderSectorsOnMap;
window.drawSectorPreview = drawSectorPreview;
window.panToSector = panToSector;
window.highlightSectorById = highlightSectorById;
window.showScanMarker = showScanMarker;
window.showSavedItemMarkers = showSavedItemMarkers;
window.clearSavedItemMarkers = clearSavedItemMarkers;

let map; // 지도 객체
let polygons = [];

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
                    const selectedText = villageSelect.options[villageSelect.selectedIndex].text;
                    areaNameDisplay.innerText = selectedText;
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
}
let map; // 지도 객체
let polygons = [];

// 1. 지도 초기화 (화면 진입 시 호출)
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

    clearPolygons();

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

    clearPolygons();

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

// 5. 읍/면/동 선택 핸들러 (폴리곤 그리기)
async function handleVillageChange() {
    const villageId = document.getElementById('villageSelect').value;
    const villageSelect = document.getElementById('villageSelect'); // select 요소 가져오기

    // (NEW) 오버레이 요소 가져오기
    const overlay = document.getElementById('areaConfirmOverlay');
    const areaNameDisplay = document.getElementById('selectedAreaName');

    clearPolygons();
    // 초기화 시 오버레이 숨김
    if(overlay) overlay.style.display = 'block';

    if (!villageId) return;

    try {
        console.log(`[Loading] Village ID: ${villageId}`);
        const res = await fetch(`${API_AREA}/villages/${villageId}/map`, { headers: getHeaders() });

        if (res.ok) {
            const data = await res.json();

            if (typeof kakao === 'undefined' || !kakao.maps) {
                alert("카카오맵 로드 실패");
                return;
            }

            if (data.points && data.points.length > 0) {
                const path = data.points.map(p => new kakao.maps.LatLng(p.y, p.x));
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
                map.panTo(path[0]); // 폴리곤 중심으로 지도 이동

                // (NEW) 폴리곤 로드 성공 시 하단 오버레이 표시
                if (overlay && areaNameDisplay) {
                    // 선택된 옵션의 텍스트(동 이름) 가져오기
                    const selectedText = villageSelect.options[villageSelect.selectedIndex].text;
                    areaNameDisplay.innerText = selectedText;

                    overlay.style.display = 'block'; // 오버레이 등장!
                }
            }
        } else {
            alert("구역 정보 로드 실패");
        }
    } catch (e) {
        console.error(e);
        // 에러 발생 시에도 오버레이는 띄우지 않음
    }
}
function clearPolygons() {
    polygons.forEach(p => p.setMap(null));
    polygons = [];
}
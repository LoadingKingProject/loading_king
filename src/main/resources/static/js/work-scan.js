(() => {
    const video = document.getElementById("cameraPreview");
    const overlay = document.getElementById("cameraOverlay");
    const overlayVideo = document.getElementById("cameraOverlayPreview");
    const overlayResult = document.getElementById("cameraOverlayResult");
    const toggleOverlayBtn = document.getElementById("btnToggleOverlay");
    const showOverlayBtn = document.getElementById("btnShowOverlay");
    const lastAssigned = document.getElementById("scanLastAssigned");
    const lastAssignedValue = document.getElementById("scanLastAssignedValue");
    const qrCanvas = document.getElementById("qrCanvas");
    const qrCtx = qrCanvas ? qrCanvas.getContext("2d", { willReadFrequently: true }) : null;
    const placeholder = document.getElementById("cameraPlaceholder");
    const badge = document.getElementById("scanStatusBadge");

    const btnStart = document.getElementById("btnStartCamera");
    const btnStop = document.getElementById("btnStopCamera");
    const btnSim = document.getElementById("btnSimulateScan");
    const btnFinishAssign = document.getElementById("btnFinishAssign");
    const btnCompleteOrder = document.getElementById("btnCompleteOrder");

    const popup = document.getElementById("assignedPopup");
    const popupSector = document.getElementById("assignedPopupSector");
    const orderList = document.getElementById("scanOrderList");
    const unassignedList = document.getElementById("scanUnassignedList");
    const unassignedHint = document.getElementById("scanUnassignedHint");

    const resultCard = document.getElementById("scanResultCard");
    const resultTitle = document.getElementById("scanResultTitle");
    const resultDesc = document.getElementById("scanResultDesc");
    const resultRaw = document.getElementById("scanResultRaw");

    const totalCountEl = document.getElementById("scanTotalCount");
    const hintEl = document.getElementById("scanHint");
    const savedItemList = document.getElementById("savedItemList");
    const btnRefreshSavedItems = document.getElementById("btnRefreshSavedItems");
    const btnGoOrderPhase = document.getElementById("btnGoOrderPhase");

    let stream = null;
    let scanning = false;
    let detector = null;
    let zxingReader = null;
    let zxingControls = null;
    let overlayHidden = false;
    let popupTimer = null;

    let totalCount = 0;
    let lastRaw = null;
    let lastScanAt = 0;
    const DUPLICATE_COOLDOWN_MS = 1500;

    const assignedSectors = [];
    const unassignedItems = [];
    let savedItems = [];
    let sectorNameById = {};

    function setHint(msg, isError = false) {
        if (!hintEl) return;
        hintEl.textContent = msg;
        hintEl.style.color = isError ? "var(--danger-color)" : "var(--text-sub)";
    }

    function setBadge(text) {
        if (!badge) return;
        badge.style.display = "inline-block";
        badge.textContent = text;
    }

    function updateFinishAssignButton() {
        if (!btnFinishAssign) return;
        const hasSessionAssigned = assignedSectors.length > 0;
        const hasSavedAssigned = savedItems.some((item) => item?.sectorId != null);
        btnFinishAssign.disabled = !(hasSessionAssigned || hasSavedAssigned);
    }

    function showResult(statusText, detailText, raw, tone) {
        if (!resultCard || !resultTitle || !resultDesc || !resultRaw) return;
        const toneColor = tone ?? "var(--accent-color)";
        resultCard.style.display = "block";
        resultCard.style.borderColor = toneColor;
        resultTitle.style.color = toneColor;
        resultTitle.textContent = statusText ?? "-";
        resultDesc.textContent = detailText ?? "-";
        resultRaw.textContent = raw ? `RAW: ${raw}` : "";
    }

    function showOverlayResult(raw, format) {
        if (!overlayResult) return;
        if (!raw) {
            overlayResult.textContent = "";
            return;
        }
        const label = format ? `[${format}]` : "[scan]";
        overlayResult.textContent = `${label} ${raw}`;
    }

    function showAssignedPopup(sectorName) {
        if (!popup || !popupSector) return;
        if (popupTimer) clearTimeout(popupTimer);
        popupSector.textContent = sectorName || "-";
        popup.style.display = "block";
        popupTimer = setTimeout(() => {
            popup.style.display = "none";
        }, 500);
    }

    function updateOverlayVisibility() {
        if (!overlay) return;
        overlay.style.display = overlayHidden ? "none" : "flex";
        if (toggleOverlayBtn) toggleOverlayBtn.textContent = overlayHidden ? "카메라 보기" : "지도 보기";
        if (showOverlayBtn) showOverlayBtn.style.display = overlayHidden ? "inline-flex" : "none";
    }

    function incrementCount() {
        totalCount += 1;
        if (totalCountEl) totalCountEl.textContent = `${totalCount}개`;
    }

    function renderSavedItemList(items) {
        if (!savedItemList) return;
        if (!Array.isArray(items) || items.length === 0) {
            savedItemList.innerHTML = `<div>저장된 Item이 없습니다.</div>`;
            return;
        }
        savedItemList.innerHTML = items
            .map((item, idx) => `<div>${idx + 1}. ${item.barcode ?? "-"}</div>`)
            .join("");
    }

    async function fetchSavedItemsForActiveJob() {
        const headers = (typeof getHeaders === "function")
            ? getHeaders()
            : { "Content-Type": "application/json" };
        const res = await fetch(`${API_BASE}/logistics/items/active`, {
            method: "GET",
            headers
        });
        if (!res.ok) {
            const text = await res.text().catch(() => "");
            throw new Error(`${res.status} ${text}`);
        }
        return res.json();
    }

    async function refreshSavedItems() {
        if (savedItemList) savedItemList.innerHTML = `<div>불러오는 중...</div>`;
        try {
            const items = await fetchSavedItemsForActiveJob();
            savedItems = Array.isArray(items) ? items : [];
            renderSavedItemList(savedItems);
            updateFinishAssignButton();
            if (typeof window.showSavedItemMarkers === "function") {
                window.showSavedItemMarkers(savedItems);
            }
        } catch (e) {
            console.error(e);
            if (savedItemList) savedItemList.innerHTML = `<div>저장 Item 조회 실패</div>`;
            updateFinishAssignButton();
        }
    }

    async function fetchSectorNameMap() {
        const headers = (typeof getHeaders === "function")
            ? getHeaders()
            : { "Content-Type": "application/json" };
        const res = await fetch(`${API_BASE}/sectors`, {
            method: "GET",
            headers
        });
        if (!res.ok) return {};
        const sectors = await res.json();
        if (!Array.isArray(sectors)) return {};
        return sectors.reduce((acc, sector) => {
            if (sector?.id != null) acc[sector.id] = sector.sectorName ?? `Sector-${sector.id}`;
            return acc;
        }, {});
    }

    async function fillAssignedSectorsFromSavedItems() {
        if (assignedSectors.length > 0) return;

        // 1. 모든 섹터 목록 가져오기
        const headers = (typeof getHeaders === "function")
            ? getHeaders()
            : { "Content-Type": "application/json" };
        let allSectors = [];
        try {
            const res = await fetch(`${API_BASE}/sectors`, { method: "GET", headers });
            if (res.ok) {
                allSectors = await res.json();
                if (!Array.isArray(allSectors)) allSectors = [];
            }
        } catch (e) {
            console.error("Failed to fetch sectors:", e);
        }

        // 2. savedItems에서 sectorId별 count 계산
        const countBySectorId = new Map();
        savedItems.forEach((item) => {
            if (item?.sectorId == null) return;
            const prev = countBySectorId.get(item.sectorId) ?? 0;
            countBySectorId.set(item.sectorId, prev + 1);
        });

        // 3. 모든 섹터를 assignedSectors에 추가 (count와 함께)
        allSectors.forEach((sector) => {
            if (sector?.id == null) return;
            assignedSectors.push({
                sectorId: sector.id,
                sectorName: sector.sectorName ?? `Sector-${sector.id}`,
                count: countBySectorId.get(sector.id) ?? 0
            });
        });

        // sectorNameById도 업데이트 (다른 곳에서 사용할 수 있으므로)
        sectorNameById = allSectors.reduce((acc, sector) => {
            if (sector?.id != null) acc[sector.id] = sector.sectorName ?? `Sector-${sector.id}`;
            return acc;
        }, {});
    }

    function addAssignedSector(result) {
        const sectorId = result?.sectorId ?? null;
        const sectorName = result?.sectorName ?? "미지정";
        const existing = assignedSectors.find((item) => item.sectorId === sectorId);
        if (existing) {
            existing.count += 1;
            return;
        }
        assignedSectors.push({ sectorId, sectorName, count: 1 });
    }

    function addUnassigned(raw) {
        unassignedItems.push({ raw, scannedAt: new Date().toISOString() });
    }

    function renderOrderEditor() {
        if (!orderList) return;
        if (assignedSectors.length === 0) {
            orderList.innerHTML = `<p style="color:var(--text-sub);">배정된 섹터가 없습니다.</p>`;
            return;
        }

        orderList.innerHTML = assignedSectors.map((item, idx) => `
            <div class="list-item">
                <div style="display:flex; align-items:center;">
                    <span style="color:var(--primary-color); font-weight:700; margin-right:12px;">${idx + 1}</span>
                    <div>
                        <div style="font-weight:700;">${item.sectorName}</div>
                        <div style="font-size:0.85rem; color:var(--text-sub);">스캔 ${item.count}건</div>
                    </div>
                </div>
                <div class="scan-order-controls">
                    <button class="btn btn-dark scan-order-btn" data-dir="up" data-idx="${idx}" type="button">위</button>
                    <button class="btn btn-dark scan-order-btn" data-dir="down" data-idx="${idx}" type="button">아래</button>
                </div>
            </div>
        `).join("");
    }

    function renderUnassignedList() {
        if (!unassignedList || !unassignedHint) return;
        if (unassignedItems.length === 0) {
            unassignedList.textContent = "미배정 항목 없음";
            unassignedHint.textContent = "";
            return;
        }
        unassignedList.innerHTML = unassignedItems
            .slice(-5)
            .reverse()
            .map((item, idx) => `<div>${idx + 1}. ${item.raw || "-"}</div>`)
            .join("");
        unassignedHint.textContent = "해당 배송지역(Village) 내 매핑 가능한 Sector가 없으면 미배정으로 분류됩니다.";
    }

    function moveAssignedSector(idx, direction) {
        if (direction === "up" && idx > 0) {
            [assignedSectors[idx - 1], assignedSectors[idx]] = [assignedSectors[idx], assignedSectors[idx - 1]];
        } else if (direction === "down" && idx < assignedSectors.length - 1) {
            [assignedSectors[idx], assignedSectors[idx + 1]] = [assignedSectors[idx + 1], assignedSectors[idx]];
        }
        renderOrderEditor();
    }

    async function postScanItem(raw) {
        const headers = (typeof getHeaders === "function")
            ? getHeaders()
            : { "Content-Type": "application/json" };
        const res = await fetch(`${API_BASE}/logistics/scan`, {
            method: "POST",
            headers,
            body: JSON.stringify({ barcode: raw })
        });

        if (!res.ok) {
            const text = await res.text().catch(() => "");
            throw new Error(`${res.status} ${text}`);
        }
        return res.json();
    }

    async function handleRaw(raw, format) {
        const now = Date.now();
        if (raw === lastRaw && now - lastScanAt < DUPLICATE_COOLDOWN_MS) {
            setBadge("중복 스캔 무시");
            setHint("같은 코드가 너무 빨리 연속 인식되어 무시했습니다.");
            return;
        }
        lastRaw = raw;
        lastScanAt = now;

        try {
            setBadge("처리중...");
            showOverlayResult(raw, format);
            const result = await postScanItem(raw);
            incrementCount();

            const assigned = !!result.assigned;
            const sectorLabel = result.sectorName ?? "미배정";
            if (assigned) {
                addAssignedSector(result);
                updateFinishAssignButton();
                showAssignedPopup(sectorLabel);
                showResult("배정 완료", sectorLabel, raw, "var(--success-color)");
                setBadge("배정 완료");
                setHint("배정이 완료되었습니다.");
                if (lastAssigned && lastAssignedValue) {
                    lastAssigned.style.display = "block";
                    lastAssignedValue.textContent = sectorLabel;
                }
            } else {
                addUnassigned(raw);
                showResult("미배정", "구역 매핑 없음", raw, "var(--accent-color)");
                setBadge("미배정");
                setHint("해당 배송지역(Village) 내 매핑 가능한 Sector가 없습니다.");
            }

            renderUnassignedList();
            refreshSavedItems();
            if (assigned && typeof window.highlightSectorById === "function" && result.sectorId != null) {
                window.highlightSectorById(result.sectorId);
            }
            if (result.lat != null && result.lng != null && typeof window.showScanMarker === "function") {
                window.showScanMarker(result.lat, result.lng);
            }
        } catch (e) {
            console.error(e);
            showResult("처리 실패", "네트워크 또는 서버 오류", raw, "var(--danger-color)");
            setBadge("처리 실패");
            setHint("네트워크 또는 서버 오류로 처리하지 못했습니다. 다시 시도하세요.", true);
        }
    }

    function getVideoConstraints() {
        return {
            video: {
                facingMode: { ideal: "environment" },
                width: { ideal: 1920 },
                height: { ideal: 1080 },
                advanced: [{ focusMode: "continuous" }]
            },
            audio: false
        };
    }

    async function startCamera() {
        if (scanning || stream || zxingControls) return;
        try {
            setHint("카메라 권한 요청중...");
            setBadge("권한 요청");

            if (window.ZXingBrowser && window.ZXingBrowser.BrowserQRCodeReader) {
                await startZxingCamera();
                return;
            }

            stream = await navigator.mediaDevices.getUserMedia(getVideoConstraints());
            video.srcObject = stream;
            if (overlayVideo) overlayVideo.srcObject = stream;
            await video.play();

            video.style.display = "block";
            if (placeholder) placeholder.style.display = "none";
            if (btnStart) btnStart.disabled = true;
            if (btnStop) btnStop.disabled = false;
            if (overlay) {
                overlayHidden = false;
                updateOverlayVisibility();
            }
            if (overlayVideo) {
                try { await overlayVideo.play(); } catch (e) {}
            }

            setHint("카메라 시작됨. 바코드를 비추세요.");
            setBadge("스캔 대기");

            if ("BarcodeDetector" in window) {
                detector = new BarcodeDetector({
                    formats: ["qr_code", "code_128", "ean_13", "ean_8", "code_39", "upc_a", "upc_e"]
                });
            }
            scanning = true;
            scanLoop();
        } catch (e) {
            console.error(e);
            setHint(`카메라 시작 실패: ${e.message}`, true);
            setBadge("실패");
        }
    }

    async function startZxingCamera() {
        const hints = new Map();
        const ZX = window.ZXingBrowser;
        if (ZX?.DecodeHintType && ZX?.BarcodeFormat) {
            hints.set(ZX.DecodeHintType.TRY_HARDER, true);
            hints.set(ZX.DecodeHintType.POSSIBLE_FORMATS, [
                ZX.BarcodeFormat.QR_CODE,
                ZX.BarcodeFormat.DATA_MATRIX,
                ZX.BarcodeFormat.AZTEC,
                ZX.BarcodeFormat.PDF_417
            ]);
        }

        zxingReader = new window.ZXingBrowser.BrowserMultiFormatReader(hints);
        const targetVideo = overlayVideo || video;

        if (overlay) overlay.style.display = "flex";
        targetVideo.style.display = "block";
        if (placeholder) placeholder.style.display = "none";
        if (btnStart) btnStart.disabled = true;
        if (btnStop) btnStop.disabled = false;

        setHint("카메라 시작됨. 바코드를 비추세요.");
        setBadge("스캔 대기");

        try {
            if (typeof zxingReader.decodeFromConstraints === "function") {
                zxingControls = await zxingReader.decodeFromConstraints(
                    getVideoConstraints(),
                    targetVideo,
                    (result) => {
                        if (result?.getText) handleRaw(result.getText(), "zxing");
                    }
                );
                return;
            }

            const devices = await window.ZXingBrowser.BrowserCodeReader.listVideoInputDevices();
            const deviceId = devices?.[0]?.deviceId ?? null;
            zxingControls = await zxingReader.decodeFromVideoDevice(deviceId, targetVideo, (result) => {
                if (result?.getText) handleRaw(result.getText(), "zxing");
            });
        } catch (e) {
            console.error("ZXing start error:", e);
            zxingReader?.reset?.();
            zxingReader = null;
            zxingControls = null;
            stream = await navigator.mediaDevices.getUserMedia(getVideoConstraints());
            video.srcObject = stream;
            if (overlayVideo) overlayVideo.srcObject = stream;
            await video.play();
            video.style.display = "block";
            if (placeholder) placeholder.style.display = "none";
            if (overlay) overlay.style.display = "flex";
            scanning = true;
            scanLoop();
        }
    }

    function stopCamera() {
        scanning = false;

        if (zxingControls) {
            zxingControls.stop();
            zxingControls = null;
        }
        if (zxingReader) {
            zxingReader.reset?.();
            zxingReader = null;
        }
        if (stream) {
            stream.getTracks().forEach((t) => t.stop());
            stream = null;
        }

        video.srcObject = null;
        if (overlayVideo) overlayVideo.srcObject = null;
        video.style.display = "none";
        if (placeholder) placeholder.style.display = "block";
        if (overlay) {
            // 스캔 phase를 벗어날 때는 오버레이를 강제로 닫아 다음 phase가 가려지지 않게 한다.
            overlayHidden = true;
            overlay.style.display = "none";
        }
        if (showOverlayBtn) showOverlayBtn.style.display = "none";
        if (btnStart) btnStart.disabled = false;
        if (btnStop) btnStop.disabled = true;

        setHint("카메라가 중지되었습니다.");
        setBadge("중지됨");
    }

    function tryDecodeQrWithJsQr() {
        if (!qrCanvas || !qrCtx || !video.videoWidth || !video.videoHeight) return null;
        qrCanvas.width = video.videoWidth;
        qrCanvas.height = video.videoHeight;
        qrCtx.drawImage(video, 0, 0, qrCanvas.width, qrCanvas.height);
        const imageData = qrCtx.getImageData(0, 0, qrCanvas.width, qrCanvas.height);
        if (typeof jsQR !== "function") return null;
        const code = jsQR(imageData.data, imageData.width, imageData.height, { inversionAttempts: "attemptBoth" });
        return code ? code.data : null;
    }

    async function scanLoop() {
        if (!scanning) return;
        try {
            if (detector) {
                const barcodes = await detector.detect(video);
                if (barcodes && barcodes.length > 0) {
                    const first = barcodes[0];
                    const raw = first.rawValue;
                    const format = first.format;
                    if (raw) {
                        await handleRaw(raw, format);
                        requestAnimationFrame(scanLoop);
                        return;
                    }
                }
            }

            const qr = tryDecodeQrWithJsQr();
            if (qr) await handleRaw(qr, "qr_code");
        } catch (e) {
            console.warn("detect error:", e);
        }
        requestAnimationFrame(scanLoop);
    }

    function simulate() {
        const sample =
            "|V1|10305871128210|송파구 백제고분로 40길 24|정봄빌딩||3층|정*준|499b8997|37.502565008848094|127.10704897921262|B36|1764280800000|";
        handleRaw(sample, "simulation");
    }

    function completeAssign() {
        (async () => {
            try {
                await fillAssignedSectorsFromSavedItems();
            } catch (e) {
                console.error("fillAssignedSectors error:", e);
            }
            if (assignedSectors.length === 0) {
                setHint("배정된 섹터가 없어 순서 지정 목록이 비어 있습니다.", true);
            }
            if (typeof goToPhase === "function") {
                goToPhase("op_scan_order");
            }
        })();
    }

    function goOrderPhase() {
        completeAssign();
    }

    function completeOrder() {
        if (assignedSectors.length === 0) return;
        window.scanDeliveryOrder = assignedSectors.map((item) => ({
            sectorId: item.sectorId,
            sectorName: item.sectorName,
            count: item.count
        }));
        if (typeof goToPhase === "function") {
            goToPhase("op_load");
        }
    }

    orderList?.addEventListener("click", (event) => {
        const button = event.target.closest(".scan-order-btn");
        if (!button) return;
        const idx = Number(button.dataset.idx);
        const dir = button.dataset.dir;
        if (!Number.isInteger(idx)) return;
        moveAssignedSector(idx, dir);
    });

    btnStart?.addEventListener("click", startCamera);
    btnStop?.addEventListener("click", stopCamera);
    btnSim?.addEventListener("click", simulate);
    btnFinishAssign?.addEventListener("click", completeAssign);
    btnCompleteOrder?.addEventListener("click", completeOrder);
    btnGoOrderPhase?.addEventListener("click", goOrderPhase);
    btnRefreshSavedItems?.addEventListener("click", refreshSavedItems);
    toggleOverlayBtn?.addEventListener("click", () => {
        overlayHidden = !overlayHidden;
        updateOverlayVisibility();
    });
    showOverlayBtn?.addEventListener("click", () => {
        overlayHidden = false;
        updateOverlayVisibility();
    });

    window.startScanCamera = startCamera;
    window.stopScanCamera = stopCamera;
    window.loadSavedItemsForScan = refreshSavedItems;
    window.renderScanOrderEditor = () => {
        renderOrderEditor();
        renderUnassignedList();
    };

    refreshSavedItems();
})();

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

    const resultCard = document.getElementById("scanResultCard");
    const resultTitle = document.getElementById("scanResultTitle");
    const resultDesc = document.getElementById("scanResultDesc");
    const resultRaw = document.getElementById("scanResultRaw");

    const totalCountEl = document.getElementById("scanTotalCount");
    const hintEl = document.getElementById("scanHint");

    let stream = null;
    let scanning = false;
    let detector = null;
    let zxingReader = null;
    let zxingControls = null;

    let totalCount = 0;
    let lastRaw = null;
    let lastScanAt = 0;
    const DUPLICATE_COOLDOWN_MS = 1500;

    function setHint(msg, isError = false) {
        hintEl.textContent = msg;
        hintEl.style.color = isError ? "var(--danger-color)" : "var(--text-sub)";
    }

    function setBadge(text) {
        badge.style.display = "inline-block";
        badge.textContent = text;
    }

    function showResult(sectorId, sectorLabel, raw) {
        resultCard.style.display = "block";
        resultTitle.textContent = sectorId ?? "-";
        resultDesc.textContent = sectorLabel ?? "-";
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

    let overlayHidden = false;
    function updateOverlayVisibility() {
        if (!overlay) return;
        overlay.style.display = overlayHidden ? "none" : "flex";
        if (toggleOverlayBtn) toggleOverlayBtn.textContent = overlayHidden ? "카메라 보기" : "지도 보기";
        if (showOverlayBtn) showOverlayBtn.style.display = overlayHidden ? "inline-flex" : "none";
    }

    function incrementCount() {
        totalCount += 1;
        totalCountEl.textContent = `${totalCount}개`;
    }

    async function postScanItem(raw) {
        // ✅ 네 프로젝트 공통 헤더 사용(api.js)
        const headers = (typeof getHeaders === "function")
            ? getHeaders()
            : { "Content-Type": "application/json" };

        const body = {
            barcode: raw
        };

        const res = await fetch(`${API_BASE}/logistics/scan`, {
            method: "POST",
            headers,
            body: JSON.stringify(body)
        });

        if (!res.ok) {
            const text = await res.text().catch(() => "");
            throw new Error(`${res.status} ${text}`);
        }

        return res.json();
    }

    async function handleRaw(raw, format) {
        const now = Date.now();
        if (raw === lastRaw && now - lastScanAt < DUPLICATE_COOLDOWN_MS) return;
        lastRaw = raw;
        lastScanAt = now;

        try {
            setBadge("저장중...");
            showOverlayResult(raw, format);
            const result = await postScanItem(raw);
            const assigned = !!result.assigned;
            const sectorLabel = result.sectorName ?? "-";
            showResult(sectorLabel, assigned ? "배정 완료" : "미배정", raw);
            incrementCount();
            setBadge(assigned ? "배정 완료 ✅" : "미배정");
            setHint("서버 저장 완료");
            if (assigned && typeof window.highlightSectorById === "function") {
                window.highlightSectorById(result.sectorId);
            }
            if (result.lat != null && result.lng != null && typeof window.showScanMarker === "function") {
                window.showScanMarker(result.lat, result.lng);
            }
            if (lastAssigned && lastAssignedValue) {
                lastAssigned.style.display = "block";
                lastAssignedValue.textContent = sectorLabel;
            }
        } catch (e) {
            console.error(e);
            showResult("-", "서버 저장 실패", raw);
            setBadge("스캔 성공(저장 실패)");
            setHint(`서버 저장 실패: ${e.message}`, true);
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
            placeholder.style.display = "none";
            btnStart.disabled = true;
            btnStop.disabled = false;
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
        placeholder.style.display = "none";
        btnStart.disabled = true;
        btnStop.disabled = false;

        setHint("카메라 시작됨. 바코드를 비추세요.");
        setBadge("스캔 대기");

        try {
            if (typeof zxingReader.decodeFromConstraints === "function") {
                zxingControls = await zxingReader.decodeFromConstraints(
                    getVideoConstraints(),
                    targetVideo,
                    (result) => {
                        if (result?.getText) {
                            handleRaw(result.getText(), "zxing");
                        }
                    }
                );
                return;
            }

            const devices = await window.ZXingBrowser.BrowserCodeReader.listVideoInputDevices();
            const deviceId = devices?.[0]?.deviceId ?? null;
            zxingControls = await zxingReader.decodeFromVideoDevice(deviceId, targetVideo, (result) => {
                if (result?.getText) {
                    handleRaw(result.getText(), "zxing");
                }
            });
        } catch (e) {
            console.error("ZXing start error:", e);
            zxingReader?.reset?.();
            zxingReader = null;
            zxingControls = null;
            // fallback to native path
            stream = await navigator.mediaDevices.getUserMedia(getVideoConstraints());
            video.srcObject = stream;
            if (overlayVideo) overlayVideo.srcObject = stream;
            await video.play();
            video.style.display = "block";
            placeholder.style.display = "none";
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
        placeholder.style.display = "block";
        if (overlay) {
            overlayHidden = false;
            updateOverlayVisibility();
        }

        btnStart.disabled = false;
        btnStop.disabled = true;

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
            if (qr) {
                await handleRaw(qr, "qr_code");
            }
        } catch (e) {
            // detect 에러로 루프 죽지 않게
            console.warn("detect error:", e);
        }
        requestAnimationFrame(scanLoop);
    }

    function simulate() {
        const sample =
            "|V1|10305871128210|송파구 백제고분로 40길 24|정봄빌딩||3층|정*준|499b8997|37.502565008848094|127.10704897921262|B36|1764280800000|";
        handleRaw(sample);
    }

    // 이벤트 바인딩
    btnStart?.addEventListener("click", startCamera);
    btnStop?.addEventListener("click", stopCamera);
    btnSim?.addEventListener("click", simulate);
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

})();

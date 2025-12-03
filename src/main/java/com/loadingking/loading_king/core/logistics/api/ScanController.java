package com.loadingking.loading_king.core.logistics.api;

import com.loadingking.loading_king.core.logistics.application.LogisticsService;
import com.loadingking.loading_king.core.logistics.dto.ScanRequest;
import com.loadingking.loading_king.core.logistics.dto.ScanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
public class ScanController {

    private final LogisticsService logisticsService;

    @PostMapping("/scan")
    public ResponseEntity<ScanResponse> scanInvoice(@RequestBody ScanRequest request) {
        // 1. 서비스 호출
        ScanResponse response = logisticsService.scanAndAssign(request);

        // 2. 결과 반환 (200 OK)
        return ResponseEntity.ok(response);
    }
}

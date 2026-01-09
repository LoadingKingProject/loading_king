package com.loadingking.loading_king.core.logistics.api;

import com.loadingking.loading_king.core.logistics.application.LogisticsService;
import com.loadingking.loading_king.core.logistics.dto.ScanRequestDto;
import com.loadingking.loading_king.core.logistics.dto.ScanResponseDto;
import com.loadingking.loading_king.infra.security.CustomUserDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logistics")
public class ScanController {

    private final LogisticsService logisticsService;

    public ScanController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    @PostMapping("/scan")
    public ResponseEntity<ScanResponseDto> scanItem(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestBody ScanRequestDto request) {

        // 기사님(User) 정보와 스캔 데이터(Barcode, Address)를 서비스로 전달
        ScanResponseDto response = logisticsService.registerItem(
                request.getBarcode(),
                request.getAddress(),
                userDetail.getUser()
        );

        return ResponseEntity.ok(response);
    }
}
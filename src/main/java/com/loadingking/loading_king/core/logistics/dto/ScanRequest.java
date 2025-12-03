package com.loadingking.loading_king.core.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScanRequest(
        @NotNull(message = "업무 ID는 필수입니다.")
        Long jobId,          // 현재 진행 중인 업무 ID

        @NotBlank(message = "송장 번호는 필수입니다.")
        String invoiceNo,    // 스캔된 송장 번호

        String address       // (선택) 만약 QR에 주소가 포함되어 있다면 사용
) {}

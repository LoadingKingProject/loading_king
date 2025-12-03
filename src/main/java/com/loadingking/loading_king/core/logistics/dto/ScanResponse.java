package com.loadingking.loading_king.core.logistics.dto;

public record ScanResponse(
        String invoiceNo,    // 송장 번호
        Long sectorId,       // 배정된 섹터 ID (null 가능)
        String sectorName,   // "A구역" (화면 표시용)
        String sectorColor   // "#FF0000" (화면 표시용)
) {
    // 편의 메서드: 섹터가 없을 때 기본 응답 생성
    public static ScanResponse unassigned(String invoiceNo) {
        return new ScanResponse(invoiceNo, null, "미분류", "#808080");
    }
}

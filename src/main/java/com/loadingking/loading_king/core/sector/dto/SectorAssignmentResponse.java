package com.loadingking.loading_king.core.sector.dto;

/**
 * 섹터 할당 응답 DTO
 * @param isAssigned 포함 여부
 * @param sectorName 섹터 이름
 * @param displayColor 식별 색상
 * @param message 사용자 안내 메시지
 */
public record SectorAssignmentResponse(
        boolean isAssigned,
        String sectorName,
        String displayColor,
        String message
) {}

package com.loadingking.loading_king.core.sector.dto;

import com.loadingking.loading_king.core.sector.domain.Sector;

public record SectorResponse(
        Long id,
        String sectorName,
        double lat,
        double lng
) {
    // Sector 엔티티를 받아서 DTO로 변환하는 메서드
    public static SectorResponse from(Sector sector) {
        return new SectorResponse(
                sector.getId(),
                sector.getSectorName(),
                sector.getCenterCoordinate().getY(), // 위도 (Lat)
                sector.getCenterCoordinate().getX()  // 경도 (Lng)
        );
    }
}
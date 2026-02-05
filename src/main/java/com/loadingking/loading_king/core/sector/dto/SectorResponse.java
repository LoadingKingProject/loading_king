package com.loadingking.loading_king.core.sector.dto;

import com.loadingking.loading_king.core.sector.domain.Sector;
import org.locationtech.jts.geom.Coordinate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record SectorResponse(
        Long id,
        String sectorName,
        double lat,
        double lng,
        String villageName,
        List<PointDto> boundary
) {
    public record PointDto(double lat, double lng) {}

    // Sector 엔티티를 받아서 DTO로 변환하는 메서드
    public static SectorResponse from(Sector sector) {
        List<PointDto> boundary = Arrays.stream(sector.getBoundary().getCoordinates())
                .map(SectorResponse::toPointDto)
                .collect(Collectors.toList());

        return new SectorResponse(
                sector.getId(),
                sector.getSectorName(),
                sector.getCenterCoordinate().getY(), // 위도 (Lat)
                sector.getCenterCoordinate().getX(), // 경도 (Lng)
                sector.getVillageName(),
                boundary
        );
    }

    private static PointDto toPointDto(Coordinate coordinate) {
        return new PointDto(coordinate.getY(), coordinate.getX());
    }
}

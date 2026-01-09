package com.loadingking.loading_king.core.sector.dto;

public record SectorRequest(
        String name,
        double lat,
        double lng,
        double radius
) {

}

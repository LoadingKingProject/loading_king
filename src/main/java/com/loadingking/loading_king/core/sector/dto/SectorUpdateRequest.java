package com.loadingking.loading_king.core.sector.dto;

public record SectorUpdateRequest(
        String name,
        double radius,
        Long villageId
) {
}

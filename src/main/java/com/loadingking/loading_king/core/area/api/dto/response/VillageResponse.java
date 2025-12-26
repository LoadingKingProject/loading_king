package com.loadingking.loading_king.core.area.api.dto.response;

import org.locationtech.jts.geom.MultiPolygon;

import java.util.List;

public record VillageResponse(
        Long id,
        String name,
        String code,
        Long districtId,
        List<List<List<List<Double>>>> coordinates
) {
}

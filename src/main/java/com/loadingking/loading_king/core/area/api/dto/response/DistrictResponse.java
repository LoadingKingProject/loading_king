package com.loadingking.loading_king.core.area.api.dto.response;

public record DistrictResponse(
        Long id,
        String name,
        String code,
        Long cityId
) {
}

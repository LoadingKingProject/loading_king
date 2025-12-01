package com.loadingking.loading_king.core.area.api.dto;

import com.loadingking.loading_king.core.area.domain.model.City;

public record DistrictRequest(
        String name,
        String code
) {
}

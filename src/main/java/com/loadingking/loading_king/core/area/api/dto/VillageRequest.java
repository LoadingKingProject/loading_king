package com.loadingking.loading_king.core.area.api.dto;

import java.util.List;

public record VillageRequest (
        String name,
        String code,
        List<List<List<List<Double>>>> coordinates
){
}

package com.loadingking.loading_king.core.area.api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class VillageMapResponse {
    private Long id;
    private String name;

    private List<PointDto> coordinates;

    @Getter
    @AllArgsConstructor
    public static class PointDto {
        private double lat;
        private double lng;
    }
}

package com.loadingking.loading_king.core.area.api.dto.response;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.loadingking.loading_king.core.area.domain.model.Village;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VillageResponse{

    private Long id;
    private String name;
    private String code;
    private String wkt;

    public static VillageResponse from(Village village){
        return VillageResponse.builder()
                .id(village.getId())
                .name(village.getName())
                .code(village.getCode())
                .wkt(village.getField() != null ? village.getField().toString() : null)
                .build();

    }
}



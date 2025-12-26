package com.loadingking.loading_king.core.area.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VillageRequest (

        @NotBlank(message = "동 이름은 필수 입력값입니다.")
        String name,

        @NotBlank(message = "법정동 코드는 필수 입력값입니다.")
        @Size(min = 10, max = 10, message = "10자리의 유효한 법정동 코드를 입력해주세요.")
        String code,

        @NotNull(message = "좌표는 필수 입력값입니다.")
        List<List<List<List<Double>>>> coordinates
){
}

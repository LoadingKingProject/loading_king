package com.loadingking.loading_king.core.area.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DistrictRequest(

        @NotBlank(message = "구 이름은 필수 입력값입니다.")
        String name,

        @NotBlank(message = "법정동 코드는 필수 입력값입니다.")
        @Size(min = 10, max = 10, message = "10자리의 유효한 법정동 코드를 입력해주세요.")
        String code
) {
}

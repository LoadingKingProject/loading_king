package com.loadingking.loading_king.core.logistics.application.port;

import com.loadingking.loading_king.core.logistics.domain.model.CoordinateInfo;

public interface GeoLocationPort {
    /**
     * 주소를 입력받아 좌표 정보를 반환한다.
     * @param address 변환할 주소 (예: "서울시 강남구...")
     * @return 위도(lat), 경도(lng) 정보
     */
    CoordinateInfo getCoordinate(String address);
}

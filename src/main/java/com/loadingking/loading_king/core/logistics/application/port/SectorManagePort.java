package com.loadingking.loading_king.core.logistics.application.port;

import com.loadingking.loading_king.core.logistics.domain.model.SectorInfo;
import java.util.Optional;

public interface SectorManagePort {
    /**
     * 현재 작업(Job) 내에서 좌표에 해당하는 섹터를 찾는다.
     * @param jobId 현재 작업 ID
     * @param lat 위도
     * @param lng 경도
     * @return 찾은 섹터 정보 (없을 수 있음)
     */
    Optional<SectorInfo> findAppropriateSector(Long jobId, double lat, double lng);
}

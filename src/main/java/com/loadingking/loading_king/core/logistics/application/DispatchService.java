package com.loadingking.loading_king.core.logistics.application;


import com.loadingking.loading_king.core.sector.application.SectorService;
import com.loadingking.loading_king.core.sector.domain.Sector;
import com.loadingking.loading_king.core.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.Point;
import java.awt.*;

@Service
public class DispatchService {

    private final SectorService sectorService;

    public DispatchService(SectorService sectorService) {
        this.sectorService = sectorService;
    }


    /**
     * 배정 전략 실행 (Dispatch Logic)
     * 기사의 담당 구역들과 물품의 위치를 비교하여 최적의 섹터 ID를 반환합니다.
     */
    @Transactional(readOnly = true)
    public Long dispatch(User driver, Point itemLocation) {

        var sectorIds = driver.getSectors();

        if (sectorIds == null || sectorIds.isEmpty()) {
            return null; // 운송할 섹터가 없는 경우 null 반환
        }

        return sectorService.findContainingSector(sectorIds, itemLocation)
                .map(Sector::getId)
                .orElse(null); // 해당 위치를 포함하는 섹터가 없으면 null 반환
    }
}

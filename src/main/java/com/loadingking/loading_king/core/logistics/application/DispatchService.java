package com.loadingking.loading_king.core.logistics.application;


import com.loadingking.loading_king.core.sector.application.SectorService;
import com.loadingking.loading_king.core.sector.domain.Sector;
import com.loadingking.loading_king.core.user.domain.User;
import com.loadingking.loading_king.core.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.Point;

@Service
public class DispatchService {

    private final SectorService sectorService;
    private final UserRepository userRepository;

    public DispatchService(SectorService sectorService, UserRepository userRepository) {
        this.sectorService = sectorService;
        this.userRepository = userRepository;
    }


    /**
     * 배정 전략 실행 (Dispatch Logic)
     * 기사의 담당 구역들과 물품의 위치를 비교하여 최적의 섹터 ID를 반환합니다.
     * 포함하는 섹터가 없으면 가장 가까운 섹터에 배정합니다.
     */
    @Transactional(readOnly = true)
    public Long dispatch(User driver, Point itemLocation) {

        User managed = userRepository.findById(driver.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
        var sectorIds = managed.getSectors();

        if (sectorIds == null || sectorIds.isEmpty()) {
            return null; // 운송할 섹터가 없는 경우 null 반환
        }

        // 1. 포함하는 섹터 찾기
        var containingSector = sectorService.findContainingSector(sectorIds, itemLocation);
        if (containingSector.isPresent()) {
            return containingSector.get().getId();
        }

        // 2. Fallback: 가장 가까운 섹터 찾기
        return sectorService.findNearestSector(sectorIds, itemLocation)
                .map(Sector::getId)
                .orElse(null);
    }
}

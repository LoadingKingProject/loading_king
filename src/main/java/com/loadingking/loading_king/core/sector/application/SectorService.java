package com.loadingking.loading_king.core.sector.application;

import com.loadingking.loading_king.core.sector.domain.Sector;
import com.loadingking.loading_king.core.sector.domain.SectorRepository;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SectorService {

    private final SectorRepository sectorRepository;

    public SectorService(SectorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    /**
     * 주어진 섹터 ID 목록에서 위치를 포함하는 섹터 찾기
     * @param sectorIds 섹터 ID 목록
     * @param location 확인 할 위치
     * @return 포함하는 섹터가 있으면 Optional에 담아 반환, 없으면 빈 Optional 반환
     */
    @Transactional(readOnly = true)
    public Optional<Sector> findContainingSector(List<Long> sectorIds, Point location) {
        List<Sector> sectors = sectorRepository.findAllById(sectorIds);

        return sectors.stream()
                .filter(sector -> sector.contains(location))
                .findFirst();
    }

    /**
     * 주어진 섹터 ID 목록에 해당하는 모든 섹터를 조회
     * @param sectorIds 섹터 ID 목록
     * @return 해당하는 섹터들의 리스트
     */
    @Transactional(readOnly = true)
    public List<Sector> getAllSectorsByIds(List<Long> sectorIds) {
        return sectorRepository.findAllById(sectorIds);
    }

    /**
     * 섹터 ID에 해당하는 섹터의 이름을 반환
     * @param sectorId 섹터 ID
     * @return 섹터 이름, 섹터가 없으면 "미배정" 반환
     */
    @Transactional(readOnly = true)
    public String getSectorName(Long sectorId) {
        return sectorRepository.findById(sectorId)
                .map(Sector::getSectorName)
                .orElse("미배정");
    }
}

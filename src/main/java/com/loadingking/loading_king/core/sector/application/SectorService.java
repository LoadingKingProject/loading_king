package com.loadingking.loading_king.core.sector.application;

import com.loadingking.loading_king.core.sector.domain.Sector;
import com.loadingking.loading_king.core.sector.domain.SectorRepository;
import com.loadingking.loading_king.core.sector.dto.SectorRequest;
import com.loadingking.loading_king.core.user.domain.User;
import com.loadingking.loading_king.global.util.GeometryUtils;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SectorService {

    private final SectorRepository sectorRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);


    public SectorService(SectorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    /**
     * [NEW] 섹터 생성 (원형 -> 다각형 변환 저장)
     */
    public Long createSector(SectorRequest request, User driver){

        Point center = GeometryUtils.createPoint(request.lat(), request.lng());

        Polygon boundary = createCirclePolygon(request.lat(), request.lng(), request.radius());

        Sector sector = Sector.create(request.name(), boundary, center);

        return sectorRepository.save(sector).getId();
    }
    /**
     * 중심점과 반경(m)으로 원형 Polygon 생성 (32각형 근사)
     */
    private Polygon createCirclePolygon(double lat, double lng, double radiusInMeters) {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory(geometryFactory);
        shapeFactory.setNumPoints(32); // 다각형의 점 개수 설정 (32각형)
        shapeFactory.setCentre(new org.locationtech.jts.geom.Coordinate(lng, lat)); // 순서 주의: (경도, 위도)


        double radiusInDegrees = radiusInMeters / 111000.0; // 대략적인 변환 (1도 ≈ 111km)
        shapeFactory.setSize(radiusInDegrees * 2); // 지름 설정

        return shapeFactory.createCircle();
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
    public List<Sector> findAllSectorsByIds(List<Long> sectorIds) {
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
    /**
     * 특정 기사(User)가 관리하는 모든 섹터를 조회
     * (현재는 프로토타입이라 모든 섹터를 다 보여주도록 구현합니다.)
     */
    @Transactional(readOnly = true)
    public List<Sector> findAllSectors(User user) {
        // [임시] 생성된 모든 섹터를 반환 (혼자 테스트할 때는 이게 편합니다)
        return sectorRepository.findAll();

        // [참고] 나중에 실제 기사님별로 구역을 나누게 되면 아래 코드로 교체하세요.
        // return sectorRepository.findAllById(user.getSectors());
    }
}

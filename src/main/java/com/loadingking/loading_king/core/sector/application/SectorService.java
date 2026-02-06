package com.loadingking.loading_king.core.sector.application;

import com.loadingking.loading_king.core.area.domain.model.Village;
import com.loadingking.loading_king.core.area.domain.repository.VillageRepository;
import com.loadingking.loading_king.core.sector.domain.Sector;
import com.loadingking.loading_king.core.sector.domain.SectorRepository;
import com.loadingking.loading_king.core.sector.dto.SectorRequest;
import com.loadingking.loading_king.core.sector.dto.SectorUpdateRequest;
import com.loadingking.loading_king.core.user.domain.User;
import com.loadingking.loading_king.core.user.repository.UserRepository;
import com.loadingking.loading_king.global.util.GeometryUtils;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SectorService {

    private final SectorRepository sectorRepository;
    private final VillageRepository villageRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);


    public SectorService(
            SectorRepository sectorRepository,
            VillageRepository villageRepository,
            UserRepository userRepository
    ) {
        this.sectorRepository = sectorRepository;
        this.villageRepository = villageRepository;
        this.userRepository = userRepository;
    }

    /**
     * [NEW] 섹터 생성 (원형 -> 다각형 변환 저장)
     */
    public Sector createSector(SectorRequest request, User driver){

        Point center = GeometryUtils.createPoint(request.lng(), request.lat());
        Polygon boundary = createCirclePolygon(request.lat(), request.lng(), request.radius());
        Polygon clipped = clipToVillageIfNeeded(boundary, request.villageId());

        Point sectorCenter = (clipped != null) ? clipped.getCentroid() : center;
        Polygon sectorBoundary = (clipped != null) ? clipped : boundary;

        Sector sector = Sector.create(request.name(), sectorBoundary, sectorCenter);
        sector.updateVillageId(request.villageId());
        sector.updateVillageName(resolveVillageName(request.villageId()));

        Sector saved = sectorRepository.save(sector);
        User managed = loadUser(driver);
        managed.addSector(saved.getId());
        return saved;
    }

    public Sector updateSector(Long sectorId, SectorUpdateRequest request, User driver) {
        User managed = loadUser(driver);
        if (!managed.getSectors().contains(sectorId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new IllegalArgumentException("섹터를 찾을 수 없습니다."));

        Point center = sector.getCenterCoordinate();
        Polygon boundary = createCirclePolygon(center.getY(), center.getX(), request.radius());
        Polygon clipped = clipToVillageIfNeeded(boundary, request.villageId());
        Polygon updatedBoundary = (clipped != null) ? clipped : boundary;
        Point updatedCenter = updatedBoundary.getCentroid();

        if (request.name() != null && !request.name().isBlank()) {
            sector.updateName(request.name());
        }
        sector.updateBoundary(updatedBoundary, updatedCenter);
        sector.updateVillageId(request.villageId());
        sector.updateVillageName(resolveVillageName(request.villageId()));

        return sector;
    }

    public void deleteSector(Long sectorId, User driver) {
        User managed = loadUser(driver);
        if (!managed.getSectors().contains(sectorId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        if (!sectorRepository.existsById(sectorId)) {
            throw new IllegalArgumentException("섹터를 찾을 수 없습니다.");
        }

        sectorRepository.deleteById(sectorId);
        managed.removeSector(sectorId);
    }

    public List<Sector> createSectorsFromVillage(Long villageId, int count, User driver) {
        if (count < 2) {
            throw new IllegalArgumentException("섹터 개수는 2 이상이어야 합니다.");
        }

        Village village = villageRepository.findById(villageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동입니다."));

        Geometry area = village.getField();
        Envelope envelope = area.getEnvelopeInternal();
        double minX = envelope.getMinX();
        double maxX = envelope.getMaxX();
        double minY = envelope.getMinY();
        double maxY = envelope.getMaxY();
        double width = (maxX - minX) / count;

        List<Sector> created = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            double sliceMinX = minX + (width * i);
            double sliceMaxX = minX + (width * (i + 1));

            Geometry slice = geometryFactory.toGeometry(new Envelope(sliceMinX, sliceMaxX, minY, maxY));
            Geometry clipped = area.intersection(slice);
            Polygon polygon = extractLargestPolygon(clipped);
            if (polygon == null || polygon.isEmpty()) {
                continue;
            }

            Point centroid = polygon.getCentroid();
            String name = "S" + (i + 1);
            Sector sector = Sector.create(name, polygon, centroid);
            sector.updateVillageId(village.getId());
            sector.updateVillageName(village.getName());
            created.add(sectorRepository.save(sector));
        }

        User managed = loadUser(driver);
        managed.replaceSectors(created.stream().map(Sector::getId).collect(java.util.stream.Collectors.toList()));

        return created;
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
     * 주어진 섹터 ID 목록에서 위치에 가장 가까운 섹터 찾기
     * @param sectorIds 섹터 ID 목록
     * @param location 확인 할 위치
     * @return 가장 가까운 섹터가 있으면 Optional에 담아 반환, 없으면 빈 Optional 반환
     */
    @Transactional(readOnly = true)
    public Optional<Sector> findNearestSector(List<Long> sectorIds, Point location) {
        if (location == null) {
            throw new IllegalArgumentException("location cannot be null");
        }
        if (sectorIds == null || sectorIds.isEmpty()) {
            return Optional.empty();
        }

        List<Sector> sectors = sectorRepository.findAllById(sectorIds);

        return sectors.stream()
                .min((s1, s2) -> Double.compare(
                    location.distance(s1.getBoundary()),
                    location.distance(s2.getBoundary())
                ));
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
        User managed = loadUser(user);
        return sectorRepository.findAllById(managed.getSectors());
    }

    private User loadUser(User user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
    }

    private Polygon clipToVillageIfNeeded(Polygon boundary, Long villageId) {
        if (villageId == null) {
            return null;
        }

        Village village = villageRepository.findById(villageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동입니다."));

        Geometry clipped = village.getField().intersection(boundary);
        Polygon polygon = extractLargestPolygon(clipped);
        if (polygon == null || polygon.isEmpty()) {
            throw new IllegalArgumentException("선택한 반경이 동 영역과 겹치지 않습니다.");
        }
        return polygon;
    }

    private String resolveVillageName(Long villageId) {
        if (villageId == null) {
            return null;
        }
        return villageRepository.findById(villageId)
                .map(Village::getName)
                .orElse(null);
    }


    private Polygon extractLargestPolygon(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }

        if (geometry instanceof Polygon polygon) {
            return polygon;
        }

        Polygon largest = null;
        double maxArea = -1.0;
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry part = geometry.getGeometryN(i);
            if (part instanceof Polygon polygon) {
                double area = polygon.getArea();
                if (area > maxArea) {
                    maxArea = area;
                    largest = polygon;
                }
            }
        }

        return largest;
    }
}

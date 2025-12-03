package com.loadingking.loading_king.core.logistics.application;

import com.loadingking.loading_king.core.logistics.application.port.GeoLocationPort;
import com.loadingking.loading_king.core.logistics.application.port.SectorManagePort;
import com.loadingking.loading_king.core.logistics.domain.model.CoordinateInfo;
import com.loadingking.loading_king.core.logistics.domain.model.Item;
import com.loadingking.loading_king.core.logistics.domain.model.SectorInfo;
import com.loadingking.loading_king.core.logistics.dto.ScanRequest;
import com.loadingking.loading_king.core.logistics.dto.ScanResponse;
import com.loadingking.loading_king.core.logistics.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional // 트랜잭션 보장 (예외 발생 시 롤백)
public class LogisticsService {

    private final ItemRepository itemRepository; // DB 저장소
    private final GeoLocationPort geoPort;       // 카카오 (Stub)
    private final SectorManagePort sectorPort;   // 섹터 (Stub)

    private final GeometryFactory geometryFactory = new GeometryFactory(); // Point 생성용

    /**
     * 송장을 스캔하여 섹터를 배정하고 저장한다.
     */
    public ScanResponse scanAndAssign(ScanRequest request) {
        // 1. 물품 조회 또는 생성 (송장번호 기준)
        Item item = itemRepository.findByInvoiceNo(request.invoiceNo())
                .orElseGet(() -> Item.create(
                        request.jobId(), // (주의) Item 엔티티 수정 필요: userId -> jobId로 변경했는지 확인
                        request.invoiceNo(),
                        request.address()
                ));

        // 2. 주소 -> 좌표 변환 (Port 호출)
        CoordinateInfo coordInfo = geoPort.getCoordinate(item.getAddress());

        // JTS Point 객체 생성 (MySQL 저장용)
        Point location = geometryFactory.createPoint(new Coordinate(coordInfo.lng(), coordInfo.lat()));
        item.updateDestination(location); // 엔티티 업데이트

        // 3. 섹터 판별 (Port 호출)
        // 섹터를 못 찾으면 예외를 던지거나 "미분류" 처리 (여기선 예외 대신 null 처리 예시)
        SectorInfo sectorInfo = sectorPort.findAppropriateSector(
                request.jobId(),
                coordInfo.lat(),
                coordInfo.lng()
        ).orElse(null);

        // 4. 배정 및 저장
        if (sectorInfo != null) {
            item.assignSector(sectorInfo.id());
        }

        itemRepository.save(item); // 저장

        // 5. 결과 반환 (DTO)
        return new ScanResponse(
                item.getInvoiceNo(),
                sectorInfo != null ? sectorInfo.id() : null,
                sectorInfo != null ? sectorInfo.name() : "미분류",
                sectorInfo != null ? sectorInfo.color() : "#CCCCCC" // 회색
        );
    }
}

/**
 * 송장을 스캔해서 -> 좌표 구하고 -> 섹터 찾아서 -> 저장하고 -> 결과를 리턴
 */
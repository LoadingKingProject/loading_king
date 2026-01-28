package com.loadingking.loading_king.core.logistics.application;

import com.loadingking.loading_king.core.logistics.domain.model.Item;
import com.loadingking.loading_king.core.logistics.repository.ItemRepository;
import com.loadingking.loading_king.core.logistics.domain.model.DeliveryJob;
import com.loadingking.loading_king.core.logistics.repository.DeliveryJobRepository;
import com.loadingking.loading_king.core.sector.application.SectorService;
import com.loadingking.loading_king.core.logistics.dto.ScanResponseDto;
import com.loadingking.loading_king.global.util.GeometryUtils;
import com.loadingking.loading_king.core.user.domain.User;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;


@Service
public class LogisticsService {
    private final ItemRepository itemRepository;
    private final DeliveryJobRepository deliveryJobRepository;
    private final SectorService sectorService;
    private final DispatchService dispatchService;  // [NEW] 배정 전담 서비스

    public LogisticsService(
            ItemRepository itemRepository,
            DeliveryJobRepository deliveryJobRepository,
            SectorService sectorService,
            DispatchService dispatchService
    ) {
        this.itemRepository = itemRepository;
        this.deliveryJobRepository = deliveryJobRepository;
        this.sectorService = sectorService;
        this.dispatchService = dispatchService;
    }

    @Transactional
    public ScanResponseDto registerItem(String barcode, String address, User user) {
        // 1. 중복 체크
        if (itemRepository.findByBarcode(barcode).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 바코드입니다.");
        }
        // 2. 바코드에서 좌표/주소 파싱
        ParsedBarcode parsed = parseBarcode(barcode, address);
        if (parsed.lat == null || parsed.lng == null) {
            throw new IllegalArgumentException("바코드에 좌표 정보가 없습니다.");
        }
        Point location = GeometryUtils.createPoint(parsed.lng, parsed.lat);

        // 3. [Refactored] 섹터 배정 (DispatchService에 위임)
        // LogisticsService는 "어떻게" 배정하는지 알 필요 없이 결과만 받아옵니다.
        Long sectorId = dispatchService.dispatch(user, location);


        // 4. 업무 조회 또는 생성
        DeliveryJob job = deliveryJobRepository.findActiveJobByDriverId(user.getId())
                .orElseGet(() -> {
                    DeliveryJob newJob = DeliveryJob.create(user.getId());
                    return deliveryJobRepository.save(newJob);
                });

        // 5. Item 저장
        String resolvedAddress = (parsed.address != null && !parsed.address.isBlank()) ? parsed.address : "미상";
        Item item = Item.create(barcode, resolvedAddress, location, sectorId, job.getId());
        itemRepository.save(item);


        // 6. 결과 반환 (이름 조회는 SectorService 이용)
        String sectorName = (sectorId != null) ? sectorService.getSectorName(sectorId) : "미배정";
        boolean isAssigned = (sectorId != null);

        return new ScanResponseDto(barcode, sectorId, sectorName, isAssigned, parsed.lat, parsed.lng);
    }

    private ParsedBarcode parseBarcode(String raw, String fallbackAddress) {
        if (raw == null || raw.isBlank()) {
            return new ParsedBarcode(null, null, fallbackAddress);
        }

        String[] tokens = raw.split("\\|");
        List<String> parts = new ArrayList<>();
        for (String token : tokens) {
            if (token == null) continue;
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) parts.add(trimmed);
        }

        String parsedAddress = fallbackAddress;
        if (parts.size() > 2 && !parts.get(2).isBlank()) {
            parsedAddress = parts.get(2);
        }

        Double lat = null;
        Double lng = null;

        // 우선: 고정 포맷(|) 기준 index 6,7 (0-based)
        Double fixedLat = parseDoubleSafe(parts.size() > 6 ? parts.get(6) : null);
        Double fixedLng = parseDoubleSafe(parts.size() > 7 ? parts.get(7) : null);
        if (isLat(fixedLat) && isLng(fixedLng)) {
            lat = fixedLat;
            lng = fixedLng;
        } else if (isLat(fixedLng) && isLng(fixedLat)) {
            lat = fixedLng;
            lng = fixedLat;
        }

        // 폴백: 연속 숫자 쌍에서 위/경도 범위로 추출
        if (lat == null || lng == null) {
            for (int i = 0; i < parts.size() - 1; i++) {
                Double first = parseDoubleSafe(parts.get(i));
                Double second = parseDoubleSafe(parts.get(i + 1));
                if (first == null || second == null) {
                    continue;
                }
                if (isLat(first) && isLng(second)) {
                    lat = first;
                    lng = second;
                    break;
                }
                if (isLat(second) && isLng(first)) {
                    lat = second;
                    lng = first;
                    break;
                }
            }
        }

        return new ParsedBarcode(lat, lng, parsedAddress);
    }

    private Double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean isLat(Double value) {
        return value != null && value >= -90.0 && value <= 90.0;
    }

    private boolean isLng(Double value) {
        return value != null && value >= -180.0 && value <= 180.0;
    }

    private static class ParsedBarcode {
        private final Double lat;
        private final Double lng;
        private final String address;

        private ParsedBarcode(Double lat, Double lng, String address) {
            this.lat = lat;
            this.lng = lng;
            this.address = address;
        }
    }
}

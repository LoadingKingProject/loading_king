package com.loadingking.loading_king.core.logistics.application;

import com.loadingking.loading_king.core.logistics.domain.model.Item;
import com.loadingking.loading_king.core.logistics.repository.ItemRepository;
import com.loadingking.loading_king.core.logistics.domain.model.DeliveryJob;
import com.loadingking.loading_king.core.logistics.repository.DeliveryJobRepository;
import com.loadingking.loading_king.core.sector.application.SectorService;
import com.loadingking.loading_king.core.logistics.dto.ScanResponseDto;
import com.loadingking.loading_king.core.sector.domain.Sector;
import com.loadingking.loading_king.global.util.GeometryUtils;
import com.loadingking.loading_king.core.user.domain.User;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
public class LogisticsService {
    private final ItemRepository itemRepository;
    private final DeliveryJobRepository deliveryJobRepository;
    private final SectorService sectorService;

    public LogisticsService(ItemRepository itemRepository, DeliveryJobRepository deliveryJobRepository, SectorService sectorService) {
        this.itemRepository = itemRepository;
        this.deliveryJobRepository = deliveryJobRepository;
        this.sectorService = sectorService;
    }

    @Transactional
    public ScanResponseDto registerItem(String barcode, String address, User user) {
        // 1. 중복 체크
        if (itemRepository.findByBarcode(barcode).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 바코드입니다.");
        }

        // 2. 좌표 변환 (임시 더미 좌표 사용)
        Point location = GeometryUtils.createPoint(0.0, 0.0);

        // 3. 섹터 판별
        List<Long> candidateSectorIds = user.getSectors();

        Long sectorId = sectorService.findContainingSector(user.getSectors(), location)
                .map(Sector::getId)
                .orElse(null);

        // 4. 업무 조회 또는 생성
        DeliveryJob job = deliveryJobRepository.findActiveJobByDriverId(user.getId())
                .orElseGet(() -> {
                    DeliveryJob newJob = DeliveryJob.create(user.getId());
                    return deliveryJobRepository.save(newJob);
                });

        // 5. Item 저장
        Item item = Item.create(barcode, address,location, sectorId, job.getId());
        itemRepository.save(item);

        // 6. 결과 반환
        String sectorName = (sectorId != null) ? sectorService.getSectorName(sectorId) : "미배정";
        boolean isAssigned = (sectorId != null);
        return new ScanResponseDto(barcode, sectorName, isAssigned);
    }
}

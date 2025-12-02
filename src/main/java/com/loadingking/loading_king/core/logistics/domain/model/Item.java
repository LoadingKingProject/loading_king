package com.loadingking.loading_king.core.logistics.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.locationtech.jts.geom.Point;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items") // DB 테이블명은 복수형 권장
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 담당 기사 ID

    @Column(name = "sector_id")
    private Long sectorId; // 배정된 섹터 (초기엔 null 가능)

    @Column(name = "invoice_no", nullable = false)
    private String invoiceNo;

    @Column(name = "address")
    private String address;

    @Column(name = "destination", columnDefinition = "POINT SRID 4326")
    private Point destination; // 좌표

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ItemStatus status; // SCANNED, LOADED

    @Column(name = "scan_order")
    private Integer scanOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // --- 생성자 (Factory Method) ---
    public static Item create(Long userId, String invoiceNo, String address) {
        Item item = new Item();
        item.userId = userId;
        item.invoiceNo = invoiceNo;
        item.address = address;
        item.status = ItemStatus.SCANNED;
        item.createdAt = LocalDateTime.now();
        return item;
    }

    // --- 비즈니스 로직 (상태 변경 메서드) ---

    // 1. 좌표 업데이트
    public void updateDestination(Point destination) {
        this.destination = destination;
    }

    // 2. 섹터 배정
    public void assignSector(Long sectorId) {
        this.sectorId = sectorId;
        // 필요하다면 상태 변경 추가 (예: status = ItemStatus.ASSIGNED)
    }
}

package com.loadingking.loading_king.core.logistics.domain.model;

import jakarta.persistence.*;
import lombok.Getter; // Lombok 추가
import org.locationtech.jts.geom.Point;

@Getter // [추가] 서비스에서 필드값 꺼내려면 필수!
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String barcode;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, columnDefinition = "POINT SRID 4326")
    private Point location;

    private Long sectorId; // Nullable (미배정 가능)

    // [수정] nullable = false 제거 -> 초기엔 배송업무 할당 전일 수 있음
    @Column(name = "delivery_job_id")
    private Long deliveryJobId;

    protected Item() {
        // JPA 기본 생성자
    }

    private Item(String barcode, String address, Point location, Long sectorId, Long deliveryJobId) {
        this.barcode = barcode;
        this.address = address;
        this.location = location;
        this.sectorId = sectorId;
        this.deliveryJobId = deliveryJobId;
    }

    public static Item create(String barcode, String address, Point location, Long sectorId, Long deliveryJobId) {
        return new Item(barcode, address, location, sectorId, deliveryJobId);
    }
}
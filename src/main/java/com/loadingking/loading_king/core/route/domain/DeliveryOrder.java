package com.loadingking.loading_king.core.route.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "delivery_orders")
public class DeliveryOrder {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long driverId; // 기사님 ID

    // 섹터 ID들의 순서를 저장하는 리스트 (순서 보장)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "delivery_order_sectors", joinColumns = @JoinColumn(name = "delivery_order_id"))
    @OrderColumn(name = "display_order") // DB에 순서 컬럼 자동 생성
    private List<Long> sectorIds = new ArrayList<>();

    public DeliveryOrder(Long driverId, List<Long> sectorIds) {
        this.driverId = driverId;
        this.sectorIds = sectorIds;
    }

    // 순서 업데이트 메서드
    public void updateOrder(List<Long> newSectorIds) {
        this.sectorIds.clear();
        this.sectorIds.addAll(newSectorIds);
    }
}
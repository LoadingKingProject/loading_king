package com.loadingking.loading_king.core.route.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<DeliveryOrder, Long> {
    // 기사님 ID로 저장된 순서를 찾는 메서드
    Optional<DeliveryOrder> findByDriverId(Long driverId);
}
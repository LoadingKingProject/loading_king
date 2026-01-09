package com.loadingking.loading_king.core.route.application;

import com.loadingking.loading_king.core.route.domain.DeliveryOrder;
import com.loadingking.loading_king.core.route.domain.RouteRepository;
import com.loadingking.loading_king.core.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    // 순서 저장 (없으면 생성, 있으면 업데이트)
    public void saveOrder(User driver, List<Long> sectorIds) {
        DeliveryOrder order = routeRepository.findByDriverId(driver.getId())
                .orElse(new DeliveryOrder(driver.getId(), sectorIds));

        if (order.getId() != null) {
            // 이미 존재하면 순서만 업데이트
            order.updateOrder(sectorIds);
        } else {
            // 없으면 새로 저장
            routeRepository.save(order);
        }
    }

    // 순서 조회
    @Transactional(readOnly = true)
    public List<Long> getOrder(User driver) {
        return routeRepository.findByDriverId(driver.getId())
                .map(DeliveryOrder::getSectorIds)
                .orElse(List.of()); // 저장된 게 없으면 빈 리스트 반환
    }
}
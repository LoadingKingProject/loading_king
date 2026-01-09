package com.loadingking.loading_king.core.route.api;

import com.loadingking.loading_king.core.route.application.RouteService;
import com.loadingking.loading_king.infra.security.CustomUserDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    // 순서 저장 (프론트: Setup Step 4 -> finalizeSetup)
    @PostMapping("/order")
    public ResponseEntity<Void> saveOrder(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestBody List<Long> sectorIds) {

        routeService.saveOrder(userDetail.getUser(), sectorIds);
        return ResponseEntity.ok().build();
    }

    // 순서 조회 (프론트: Operation Phase)
    @GetMapping("/order")
    public ResponseEntity<List<Long>> getOrder(@AuthenticationPrincipal CustomUserDetail userDetail) {
        List<Long> order = routeService.getOrder(userDetail.getUser());
        return ResponseEntity.ok(order);
    }
}
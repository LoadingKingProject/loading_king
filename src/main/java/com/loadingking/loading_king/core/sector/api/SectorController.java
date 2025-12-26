package com.loadingking.loading_king.core.sector.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sectors")
public class SectorController {

    @PostMapping
    public ResponseEntity<Void> saveSectorByPoint(@RequestParam double lat, @RequestParam double lng) {
        //TODO: 프론트에서 지도에 다각형을 그리면 그 좌표들을 서버에 저장
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<List<?>> getAllSectors(@PathVariable Long driverId) {
        //TODO: 저장된 모든 구역의 경계 좌표와 이름을 목록으로 가져옴
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/search")
    public ResponseEntity<?> findSectorByPoint(@RequestParam double lat, @RequestParam double lng) {
        //TODO: 특정 위도/경도를 보내면, 그 위치를 감싸고 있는 구역 정보를 응답
        return ResponseEntity.ok(null);
    }

    @PostMapping("/assignment")
    public ResponseEntity<?> assignSector() {
        //TODO: 배송 업무 ID를 보내면, 각 업무의 위치 정보를 계산해서 적절한 구역 ID를 반환
        return ResponseEntity.ok(null);
    }

}

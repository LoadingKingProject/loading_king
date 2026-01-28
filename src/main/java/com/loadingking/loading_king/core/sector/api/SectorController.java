package com.loadingking.loading_king.core.sector.api;

import com.loadingking.loading_king.core.sector.application.SectorService;
import com.loadingking.loading_king.core.sector.domain.Sector;
import com.loadingking.loading_king.core.sector.dto.SectorAutoRequest;
import com.loadingking.loading_king.core.sector.dto.SectorRequest;
import com.loadingking.loading_king.core.sector.dto.SectorResponse;
import com.loadingking.loading_king.core.sector.dto.SectorUpdateRequest;
import com.loadingking.loading_king.infra.security.CustomUserDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sectors") // 1. 모든 요청은 /api/sectors 로 시작합니다.
public class SectorController {

    private final SectorService sectorService;

    public SectorController(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    // 2. 섹터 생성하기 (프론트: Setup Step 2)
    // POST /api/sectors 요청이 오면 실행됩니다.
    @PostMapping
    public ResponseEntity<SectorResponse> createSector(
            @AuthenticationPrincipal CustomUserDetail userDetail, // 누가 요청했는지(기사님 정보)
            @RequestBody SectorRequest request) {                 // 프론트에서 보낸 데이터(반경, 좌표 등)

        // 서비스에게 "이 데이터로 섹터 좀 만들어줘(다각형 변환해줘)"라고 시킵니다.
        Sector sector = sectorService.createSector(request, userDetail.getUser());
        return ResponseEntity.ok(SectorResponse.from(sector));
    }

    @PostMapping("/auto")
    public ResponseEntity<List<SectorResponse>> createSectorsFromVillage(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestBody SectorAutoRequest request) {

        List<Sector> sectors = sectorService.createSectorsFromVillage(
                request.villageId(),
                request.count(),
                userDetail.getUser()
        );

        List<SectorResponse> response = sectors.stream()
                .map(SectorResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{sectorId}")
    public ResponseEntity<SectorResponse> updateSector(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable Long sectorId,
            @RequestBody SectorUpdateRequest request
    ) {
        Sector sector = sectorService.updateSector(sectorId, request, userDetail.getUser());
        return ResponseEntity.ok(SectorResponse.from(sector));
    }

    @DeleteMapping("/{sectorId}")
    public ResponseEntity<Void> deleteSector(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable Long sectorId
    ) {
        sectorService.deleteSector(sectorId, userDetail.getUser());
        return ResponseEntity.noContent().build();
    }

    // 3. 내 섹터 목록 조회하기 (프론트: Setup Step 3)
    // GET /api/sectors 요청이 오면 실행됩니다.
    @GetMapping
    public ResponseEntity<List<SectorResponse>> getMySectors(
            @AuthenticationPrincipal CustomUserDetail userDetail) {

        // 서비스에게 "이 기사님이 만든 섹터 다 가져와"라고 시킵니다.
        List<Sector> sectors = sectorService.findAllSectors(userDetail.getUser());

        List<SectorResponse> response = sectors.stream()
                .map(SectorResponse::from)
                .collect(Collectors.toList());

        // 목록을 프론트엔드에 돌려줍니다.
        return ResponseEntity.ok(response);
    }


}

package com.loadingking.loading_king.core.area.api.controller;

import com.loadingking.loading_king.core.area.api.dto.request.CityRequest;
import com.loadingking.loading_king.core.area.api.dto.response.CityResponse;
import com.loadingking.loading_king.core.area.api.dto.request.DistrictRequest;
import com.loadingking.loading_king.core.area.api.dto.request.VillageRequest;
import com.loadingking.loading_king.core.area.api.dto.response.DistrictResponse;
import com.loadingking.loading_king.core.area.api.dto.response.VillageMapResponse;
import com.loadingking.loading_king.core.area.api.dto.response.VillageResponse;
import com.loadingking.loading_king.core.area.application.AreaService;
import com.loadingking.loading_king.core.area.domain.model.Village;
import com.loadingking.loading_king.core.area.domain.repository.VillageRepository;
import jakarta.validation.Valid;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api-v1/area")
public class AreaController {

    private final AreaService areaService;
    private final VillageRepository villageRepository;

    public AreaController(AreaService areaService, VillageRepository villageRepository) {
        this.areaService = areaService;
        this.villageRepository = villageRepository;
    }

    @PostMapping("/city")
    public ResponseEntity<Long> createCity (@Valid
                                            @RequestBody CityRequest request){

        Long cityId = areaService.saveCity(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cityId);
    }

    @PostMapping("/{cityId}/district")
    public ResponseEntity<Long> createDistrict(@PathVariable Long cityId,
                                               @Valid
                                               @RequestBody DistrictRequest request){

        Long districtId = areaService.saveDistrict(cityId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(districtId);
    }

    @PostMapping("/{districtId}/village")
    public ResponseEntity<Long> createVillage(@PathVariable Long districtId,
                                              @Valid
                                              @RequestBody VillageRequest request){

        Long villageId = areaService.saveVillage(districtId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(villageId);
    }

    @GetMapping("/cities")
    public ResponseEntity<List<CityResponse>> findAllCities(){

        return ResponseEntity.ok(areaService.findAllCities());
    }

    @GetMapping("/districts")
    public ResponseEntity<List<DistrictResponse>> findAllDistricts(){

        return ResponseEntity.ok(areaService.findAllDistricts());
    }

    @GetMapping("/villages")
    public ResponseEntity<List<VillageResponse>> findAllVillages(){

        return ResponseEntity.ok(areaService.findAllVillages());
    }

    @GetMapping("/{cityId}/districts")
    public ResponseEntity<List<DistrictResponse>> findDistrictsByCityId(@PathVariable Long cityId){

        return ResponseEntity.ok(areaService.findDistrictsByCityId(cityId));
    }

    @GetMapping("/{districtId}/villages")
    public ResponseEntity<List<VillageResponse>> findVillagesByDistrictId(@PathVariable Long districtId){

        return ResponseEntity.ok(areaService.findVillagesByDistrictId(districtId));
    }

    @GetMapping("/cities/{cityId}")
    public ResponseEntity<CityResponse> findCityById(@PathVariable Long cityId){

        return ResponseEntity.ok(areaService.findCityById(cityId));
    }

    @GetMapping("/districts/{districtId}")
    public ResponseEntity<DistrictResponse> findDistrictById(@PathVariable Long districtId){

        return ResponseEntity.ok(areaService.findDistrictById(districtId));
    }

    @GetMapping("/villages/{villageId}")
    public ResponseEntity<VillageResponse> findVillageById(@PathVariable Long villageId){

        return ResponseEntity.ok(areaService.findVillageById(villageId));
    }
    @GetMapping("/villages/{villageId}/map")
    public ResponseEntity<VillageMapResponse> getVillageMap(@PathVariable Long villageId) {
        Village village = villageRepository.findById(villageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동입니다."));

        // Geometry(Polygon) -> List<PointDto> 변환 로직
        List<VillageMapResponse.PointDto> points = new ArrayList<>();
        if (village.getField() != null) {
            for (Coordinate coord : village.getField().getCoordinates()) {
                // 주의: JTS Coordinate는 (x, y) = (lng, lat) 순서일 수 있음. 확인 필요.
                // GeoJsonLoader에서 저장할 때 lat, lng 순서로 맞췄다면 그대로 사용.
                // 보통 카카오맵은 (lat, lng) 순서를 원함.
                points.add(new VillageMapResponse.PointDto(coord.x, coord.y));
            }
        }

        return ResponseEntity.ok(new VillageMapResponse(village.getId(), village.getName(), points));
    }
}

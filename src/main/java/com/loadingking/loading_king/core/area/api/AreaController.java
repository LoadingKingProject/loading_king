package com.loadingking.loading_king.core.area.api;

import com.loadingking.loading_king.core.area.api.dto.CityRequest;
import com.loadingking.loading_king.core.area.api.dto.DistrictRequest;
import com.loadingking.loading_king.core.area.api.dto.VillageRequest;
import com.loadingking.loading_king.core.area.application.AreaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-v1/area")
public class AreaController {

    private final AreaService areaService;

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    @PostMapping("/city")
    public ResponseEntity<Long> createCity (@RequestBody CityRequest request){

        Long cityId = areaService.saveCity(request);
        return ResponseEntity.ok(cityId);
    }

    @PostMapping("/{cityId}/district")
    public ResponseEntity<Long> createDistrict(@PathVariable Long cityId, @RequestBody DistrictRequest request){

        Long districtId = areaService.saveDistrict(cityId, request);

        return ResponseEntity.ok(districtId);
    }

    @PostMapping("/{districtId}/village")
    public ResponseEntity<Long> createVillage(@PathVariable Long districtId, @RequestBody VillageRequest request){

        Long villageId = areaService.saveVillage(districtId, request);

        return ResponseEntity.ok(villageId);
    }
}

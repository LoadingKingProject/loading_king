package com.loadingking.loading_king.core.area.domain.repository;

import com.loadingking.loading_king.core.area.api.dto.response.VillageResponse;
import com.loadingking.loading_king.core.area.domain.model.Village;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VillageRepository extends JpaRepository<Village,Long> {

    List<Village> findVillagesByDistrictId(Long districtId);


}

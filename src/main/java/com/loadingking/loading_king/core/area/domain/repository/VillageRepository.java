package com.loadingking.loading_king.core.area.domain.repository;

import com.loadingking.loading_king.core.area.domain.model.Village;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VillageRepository extends JpaRepository<Village,Long> {

    public List<Village> findVillagesByDistrictId(Long districtId);
}

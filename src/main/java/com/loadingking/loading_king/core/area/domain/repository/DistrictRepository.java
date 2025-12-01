package com.loadingking.loading_king.core.area.domain.repository;

import com.loadingking.loading_king.core.area.domain.model.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

}

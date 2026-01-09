package com.loadingking.loading_king.core.area.domain.repository;


import com.loadingking.loading_king.core.area.domain.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findAllByOrderByNameAsc();

}

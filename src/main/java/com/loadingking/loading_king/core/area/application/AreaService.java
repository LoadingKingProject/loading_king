package com.loadingking.loading_king.core.area.application;

import com.loadingking.loading_king.core.area.api.dto.CityRequest;
import com.loadingking.loading_king.core.area.api.dto.DistrictRequest;
import com.loadingking.loading_king.core.area.api.dto.VillageRequest;
import com.loadingking.loading_king.core.area.domain.model.City;
import com.loadingking.loading_king.core.area.domain.model.District;
import com.loadingking.loading_king.core.area.domain.model.Village;
import com.loadingking.loading_king.core.area.domain.repository.CityRepository;
import com.loadingking.loading_king.core.area.domain.repository.DistrictRepository;
import com.loadingking.loading_king.core.area.domain.repository.VillageRepository;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AreaService {

    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final VillageRepository villageRepository;

    private final GeometryFactory geometryFactory = new  GeometryFactory(
            new PrecisionModel(),
            4326);

    public AreaService(CityRepository cityRepository, DistrictRepository districtRepository,  VillageRepository villageRepository) {
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.villageRepository = villageRepository;
    }

    @Transactional
    public Long saveCity (CityRequest request){

        City city = new City(request.name(),  request.code());

        City savedCity = cityRepository.save(city);

        return savedCity.getId();
    }

    @Transactional
    public Long saveDistrict (Long cityId, DistrictRequest request){

        // city_id
        City city = cityRepository.findById(cityId).orElseThrow(() -> new RuntimeException("city not found"));

        District district = new District(city, request.name(), request.code());
        District savedDistrict = districtRepository.save(district);

        return savedDistrict.getId();
    }

    @Transactional
    public Long saveVillage (Long districtId, VillageRequest request){

        District district = districtRepository.findById(districtId).orElseThrow(() -> new RuntimeException("district not found"));

        MultiPolygon multiPolygon = toMultiPolygon(request.coordinates());

        Village village = new Village(district, request.name(), request.code(), multiPolygon);

        Village savedVillage = villageRepository.save(village);
        return savedVillage.getId();
    }

    private MultiPolygon toMultiPolygon(List<List<List<List<Double>>>> coords) {

        Polygon[] polygons = new Polygon[coords.size()];

        for (int i = 0; i < coords.size(); i++) {

            List<List<List<Double>>> polygonCoords = coords.get(i);
            if (polygonCoords == null || polygonCoords.isEmpty()) {
                throw new IllegalArgumentException("Polygon #" + i + "의 좌표가 비어 있습니다.");
            }

            LinearRing shell = toLinearRing(polygonCoords.get(0));

            int holeCount = polygonCoords.size() - 1;
            LinearRing[] holes = new LinearRing[holeCount > 0 ? holeCount : 0];

            for (int j = 1; j < polygonCoords.size(); j++) {
                holes[j - 1] = toLinearRing(polygonCoords.get(j));
            }

            polygons[i] = geometryFactory.createPolygon(shell, holes);
        }

        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(polygons);
        multiPolygon.setSRID(4326);

        return multiPolygon;
    }

    private LinearRing toLinearRing(List<List<Double>> ringCoords) {

        Coordinate[] coordinates = new Coordinate[ringCoords.size()];

        for (int k = 0; k < ringCoords.size(); k++) {
            List<Double> c = ringCoords.get(k);
            coordinates[k] = new Coordinate(c.get(0), c.get(1));
        }

        return geometryFactory.createLinearRing(coordinates);
    }
}

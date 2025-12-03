package com.loadingking.loading_king.core.area.application;

import com.loadingking.loading_king.core.area.api.dto.request.CityRequest;
import com.loadingking.loading_king.core.area.api.dto.response.CityResponse;
import com.loadingking.loading_king.core.area.api.dto.request.DistrictRequest;
import com.loadingking.loading_king.core.area.api.dto.request.VillageRequest;
import com.loadingking.loading_king.core.area.api.dto.response.DistrictResponse;
import com.loadingking.loading_king.core.area.api.dto.response.VillageResponse;
import com.loadingking.loading_king.core.area.domain.model.City;
import com.loadingking.loading_king.core.area.domain.model.District;
import com.loadingking.loading_king.core.area.domain.model.Village;
import com.loadingking.loading_king.core.area.domain.repository.CityRepository;
import com.loadingking.loading_king.core.area.domain.repository.DistrictRepository;
import com.loadingking.loading_king.core.area.domain.repository.VillageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.loadingking.loading_king.core.area.util.PolygonMapper.toCoordinateList;
import static com.loadingking.loading_king.core.area.util.PolygonMapper.toMultiPolygon;

@Service
public class AreaService {

    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final VillageRepository villageRepository;

    public AreaService(CityRepository cityRepository, DistrictRepository districtRepository,  VillageRepository villageRepository) {
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.villageRepository = villageRepository;
    }

    @Transactional
    public Long saveCity (CityRequest request){

        City savedCity = cityRepository.save(new City(
                request.name(),
                request.code()));

        return savedCity.getId();
    }

    @Transactional
    public Long saveDistrict (Long cityId, DistrictRequest request){

        City city = cityRepository.findById(cityId).orElseThrow(() ->
                new EntityNotFoundException("city not found"));

        District savedDistrict = districtRepository.save(new District(
                city,
                request.name(),
                request.code()));

        return savedDistrict.getId();
    }

    @Transactional
    public Long saveVillage (Long districtId, VillageRequest request){

        District district = districtRepository.findById(districtId).orElseThrow(() ->
                new EntityNotFoundException("district not found"));

        MultiPolygon multiPolygon = toMultiPolygon(request.coordinates());
        if(multiPolygon.isEmpty()){
            throw new IllegalStateException("coords is empty");
        }

        Village savedVillage = villageRepository.save(new Village(
                district,
                request.name(),
                request.code(),
                multiPolygon));

        return savedVillage.getId();
    }

    @Transactional
    public List<CityResponse> findAllCities() {

        return cityRepository.findAll()
                .stream()
                .map(c -> new CityResponse(
                        c.getId(),
                        c.getName(),
                        c.getCode()
                ))
                .toList();
    }

    @Transactional
    public List<DistrictResponse> findAllDistricts() {

        return districtRepository.findAll()
                .stream()
                .map(d -> new DistrictResponse(
                        d.getId(),
                        d.getName(),
                        d.getCode(),
                        d.getCity().getId()
                ))
                .toList();
    }

    @Transactional
    public List<VillageResponse> findAllVillages() {

        return villageRepository.findAll()
                .stream()
                .map(v -> new VillageResponse(
                        v.getId(),
                        v.getName(),
                        v.getCode(),
                        v.getDistrict().getId(),
                        toCoordinateList(v.getField())
                ))
                .toList();
    }

    @Transactional
    public List<DistrictResponse> findDistrictsByCityId(Long cityId) {

        cityRepository.findById(cityId)
                .orElseThrow(() -> new EntityNotFoundException("city not found"));

        return districtRepository.findDistrictByCityId(cityId)
                .stream()
                .map(d -> new DistrictResponse(
                        d.getId(),
                        d.getName(),
                        d.getCode(),
                        d.getCity().getId()
                )).toList();
    }

    @Transactional
    public List<VillageResponse> findVillagesByDistrictId(Long districtId) {

        districtRepository.findById(districtId)
                .orElseThrow(()-> new EntityNotFoundException("district not found"));

        return villageRepository.findVillagesByDistrictId(districtId)
                .stream()
                .map(v -> new VillageResponse(
                        v.getId(),
                        v.getName(),
                        v.getCode(),
                        v.getDistrict().getId(),
                        toCoordinateList(v.getField())
                )).toList();
    }

    @Transactional
    public CityResponse findCityById(Long cityId){

        City city = cityRepository.findById(cityId).orElseThrow(() ->
                new EntityNotFoundException("city not found"));

        return new CityResponse(
                city.getId(),
                city.getName(),
                city.getCode()
                );
    }

    @Transactional
    public DistrictResponse findDistrictById(Long districtId){

        District district = districtRepository.findById(districtId)
                .orElseThrow(()-> new EntityNotFoundException("district not found"));

        return new DistrictResponse(
                district.getId(),
                district.getName(),
                district.getCode(),
                district.getCity().getId()
        );
    }

    @Transactional
    public VillageResponse findVillageById(Long villageId){

        Village village = villageRepository.findById(villageId)
                .orElseThrow(()-> new EntityNotFoundException("village not found"));

        return new VillageResponse(
                village.getId(),
                village.getName(),
                village.getCode(),
                village.getDistrict().getId(),
                toCoordinateList(village.getField())
        );
    }



}

package com.meteo.geo.service;

import com.meteo.geo.entity.City;

import java.util.List;

public interface CityService {

    List<City> findAll();

    List<City> findByCountryId(Long countryId);

    City findById(Long id);

    City create(City city);

    City update(Long id, City city);

    void delete(Long id);
}

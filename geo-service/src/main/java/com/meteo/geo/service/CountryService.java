package com.meteo.geo.service;

import com.meteo.geo.entity.Country;

import java.util.List;

public interface CountryService {

    List<Country> findAll();

    Country findById(Long id);

    Country create(Country country);

    Country update(Long id, Country country);

    void delete(Long id);
}

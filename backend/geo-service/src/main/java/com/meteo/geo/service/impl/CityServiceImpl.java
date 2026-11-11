package com.meteo.geo.service.impl;

import com.meteo.geo.entity.City;
import com.meteo.geo.entity.Country;
import com.meteo.geo.exception.ResourceNotFoundException;
import com.meteo.geo.repository.CityRepository;
import com.meteo.geo.repository.CountryRepository;
import com.meteo.geo.service.CityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;

    public CityServiceImpl(CityRepository cityRepository, CountryRepository countryRepository) {
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<City> findAll() {
        return cityRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<City> findByCountryId(Long countryId) {
        return cityRepository.findByCountryId(countryId);
    }

    @Override
    @Transactional(readOnly = true)
    public City findById(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", id));
    }

    @Override
    public City create(City city) {
        city.setId(null);
        city.setCountry(resolveCountry(city.getCountry()));
        return cityRepository.save(city);
    }

    @Override
    public City update(Long id, City city) {
        City existing = findById(id);
        existing.setName(city.getName());
        existing.setLatitude(city.getLatitude());
        existing.setLongitude(city.getLongitude());
        existing.setPopulation(city.getPopulation());
        existing.setTimezone(city.getTimezone());
        existing.setCountry(resolveCountry(city.getCountry()));
        return cityRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        City existing = findById(id);
        cityRepository.delete(existing);
    }

    private Country resolveCountry(Country country) {
        if (country == null || country.getId() == null) {
            throw new IllegalArgumentException("country.id is required");
        }
        return countryRepository.findById(country.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", country.getId()));
    }
}

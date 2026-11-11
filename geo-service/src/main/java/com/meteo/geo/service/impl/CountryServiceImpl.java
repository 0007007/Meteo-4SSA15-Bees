package com.meteo.geo.service.impl;

import com.meteo.geo.entity.Country;
import com.meteo.geo.exception.ResourceNotFoundException;
import com.meteo.geo.repository.CityRepository;
import com.meteo.geo.repository.CountryRepository;
import com.meteo.geo.service.CountryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    public CountryServiceImpl(CountryRepository countryRepository, CityRepository cityRepository) {
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> findAll() {
        return countryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Country findById(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", id));
    }

    @Override
    public Country create(Country country) {
        country.setId(null);
        return countryRepository.save(country);
    }

    @Override
    public Country update(Long id, Country country) {
        Country existing = findById(id);
        existing.setName(country.getName());
        existing.setIsoCode(country.getIsoCode());
        existing.setContinent(country.getContinent());
        return countryRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Country existing = findById(id);
        if (cityRepository.existsByCountryId(id)) {
            throw new IllegalStateException("Country " + id + " still has cities: delete them first");
        }
        countryRepository.delete(existing);
    }
}

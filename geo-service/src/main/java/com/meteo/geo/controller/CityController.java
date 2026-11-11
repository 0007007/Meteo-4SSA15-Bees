package com.meteo.geo.controller;

import com.meteo.geo.entity.City;
import com.meteo.geo.service.CityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    public List<City> getAll(@RequestParam(name = "countryId", required = false) Long countryId) {
        return countryId == null ? cityService.findAll() : cityService.findByCountryId(countryId);
    }

    @GetMapping("/{id}")
    public City getById(@PathVariable("id") Long id) {
        return cityService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public City create(@Valid @RequestBody City city) {
        return cityService.create(city);
    }

    @PutMapping("/{id}")
    public City update(@PathVariable("id") Long id, @Valid @RequestBody City city) {
        return cityService.update(id, city);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        cityService.delete(id);
    }
}
